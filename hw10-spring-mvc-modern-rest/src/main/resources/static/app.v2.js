const appV2 = document.getElementById('app');

function h(tag, attrs = {}, ...children) {
    const el = document.createElement(tag);
    for (const [k, v] of Object.entries(attrs || {})) {
        if (k === 'class') el.className = v;
        else if (k.startsWith('on') && typeof v === 'function') el.addEventListener(k.slice(2).toLowerCase(), v);
        else if (v != null) el.setAttribute(k, v);
    }
    for (const c of children.flat()) {
        if (c == null) continue;
        el.appendChild(typeof c === 'string' ? document.createTextNode(c) : c);
    }
    return el;
}

class ApiError extends Error {
    constructor({status, title, detail, code, errors, instance, raw}) {
        super((title || 'ERROR') + (detail ? `: ${detail}` : ''));
        this.name = 'ApiError';
        this.status = status || 0;
        this.title = title || 'ERROR';
        this.detail = detail || '';
        this.code = code || null;
        this.errors = Array.isArray(errors) ? errors : [];
        this.instance = instance || null;
        this.raw = raw;
    }
}

async function api(path, {method = 'GET', body, headers} = {}) {
    const opts = {
        method,
        headers: {'Content-Type': 'application/json', ...(headers || {})},
        body: body != null ? JSON.stringify(body) : undefined
    };

    console.log('[API →]', method, path, body ?? null);

    let res;
    try {
        res = await fetch(path, opts);
    } catch (e) {
        console.error('[API ✖ NET]', e);
        throw new ApiError({status: 0, title: 'NETWORK_ERROR', detail: e.message});
    }

    console.log('[API ←]', res.status, res.statusText, res.url);
    const ct = res.headers.get('content-type') || '';
    const isJson = ct.includes('application/json') || ct.includes('application/problem+json');

    let payload = null;
    if (res.status !== 204 && isJson) {
        try {
            payload = await res.json();
        } catch { /* ignore */
        }
    }

    if (!res.ok) {
        const p = payload || {};
        throw new ApiError({
            status: res.status,
            title: p.title || res.statusText || `HTTP ${res.status}`,
            detail: p.detail || '',
            code: p.code || null,
            errors: p.errors || [],
            instance: p.instance || null,
            raw: p
        });
    }

    return payload;
}

function showProblem(err) {
    const box = h('div', {class: 'error'});
    const header = `${err.title || 'ERROR'}${err.status ? ` (${err.status})` : ''}`;
    box.appendChild(h('div', {}, header));
    if (err.detail) box.appendChild(h('div', {}, err.detail));

    if (Array.isArray(err.errors) && err.errors.length) {
        const ul = h('ul');
        err.errors.forEach(e => ul.appendChild(h('li', {}, `${e.field}: ${e.message}`)));
        box.appendChild(ul);
    }
    return box;
}

function clearFieldErrors(form) {
    form.querySelectorAll('[data-error-for]').forEach(n => n.textContent = '');
}

function applyFieldErrors(form, err) {
    if (!(err instanceof ApiError) || !Array.isArray(err.errors)) return false;
    let applied = false;
    err.errors.forEach(({field, message}) => {
        const node = form.querySelector(`[data-error-for="${field}"]`);
        if (node) {
            node.textContent = message;
            applied = true;
        }
    });
    return applied;
}

function humanizeProblem(err) {
    if (!(err instanceof ApiError)) return String(err?.message || err);
    if (err.code === 'DUPLICATE') return 'Такая запись уже существует.';
    if (err.code === 'ASSOCIATION_VIOLATION') return 'Проверьте выбранного автора и жанры.';
    if (err.code === 'VALIDATION') return 'Проверьте введённые данные.';
    if (err.status === 404) return 'Ресурс не найден.';
    if (err.status === 0) return 'Проблема с сетью или сервер недоступен.';
    return err.detail || err.message || 'Ошибка';
}

function renderNotFound(title, message, backHash = '#/books') {
    const wrap = h('div', {});
    wrap.appendChild(h('h1', {}, title || 'Не найдено'));
    wrap.appendChild(h('p', {}, message || 'Запрошенный ресурс не найден.'));
    wrap.appendChild(h('p', {}, h('a', {href: backHash}, '← Вернуться')));
    return wrap;
}


function showError(err) {
    return h('div', {class: 'error'}, String(err?.message || err));
}

function goto(hash) {
    location.hash = hash;
}

const routes = [];

function route(re, handler) {
    routes.push({re, handler});
}

async function render() {
    const hash = location.hash || '#/books';
    for (const r of routes) {
        const m = hash.match(r.re);
        if (m) {
            appV2.innerHTML = '';
            try {
                await r.handler(...m.slice(1));
            } catch (e) {
                if (e instanceof ApiError && e.status === 404) {
                    appV2.appendChild(renderNotFound('Не найдено', humanizeProblem(e)));
                } else {
                    appV2.appendChild(showProblem(e instanceof ApiError ? e : new ApiError({detail: String(e)})));
                }
            }
            return;
        }
    }
    appV2.innerHTML = '';
    appV2.appendChild(showError(new Error('Маршрут не найден: ' + hash)));
}

window.addEventListener('hashchange', render);
window.addEventListener('DOMContentLoaded', render);

route(/^#\/books(?:\?(.*))?$/, async (qs) => {
    const params = new URLSearchParams(qs || '');
    const page = Number(params.get('page') || 0);
    const size = Number(params.get('size') || 20);

    const pageData = await api(`/api/v1/books?page=${page}&size=${size}`);
    appV2.appendChild(h('h1', {}, 'Список книг'));

    const addBtn = h('p', {}, h('a', {href: '#/books/new'}, 'Добавить книгу'));
    appV2.appendChild(addBtn);

    const table = h('table', {},
        h('thead', {}, h('tr', {},
            h('th', {}, 'ID'),
            h('th', {}, 'Название'),
            h('th', {}, 'Автор'),
            h('th', {}, 'Жанры'),
            h('th', {}, 'Действия')
        )),
        h('tbody', {},
            pageData.content.map(b => h('tr', {},
                h('td', {}, String(b.id)),
                h('td', {}, h('a', {href: `#/books/${b.id}`}, b.title)),
                h('td', {}, b.authorFullName ?? ''),
                h('td', {}, b.genresSummary ?? ''),
                h('td', {},
                    h('a', {href: `#/books/${b.id}/edit`}, 'Редактировать'), ' ',
                    h('button', {
                        onClick: async () => {
                            if (!confirm('Удалить книгу?')) return;
                            await api(`/api/v1/books/${b.id}`, {method: 'DELETE'});
                            render();
                        }
                    }, 'Удалить')
                )
            ))
        )
    );
    appV2.appendChild(table);

    const pager = h('p', {class: 'muted'},
        `Всего: ${pageData.totalElements} • Стр. ${pageData.totalPages === 0 ? 0 : pageData.page + 1} из ${pageData.totalPages}`
    );

    if (!pageData.first) {
        pager.appendChild(
            h('a', {href: `#/books?page=${pageData.page - 1}&size=${size}`, style: 'margin-left:8px'}, '← Пред')
        );
    }
    if (!pageData.last) {
        pager.appendChild(
            h('a', {href: `#/books?page=${pageData.page + 1}&size=${size}`, style: 'margin-left:8px'}, 'След →')
        );
    }

    appV2.appendChild(pager);
});

route(/^#\/books\/(\d+)$/, async (id) => {
    const book = await api(`/api/v1/books/${id}`);
    appV2.appendChild(h('h1', {}, book.title));
    appV2.appendChild(h('p', {}, h('b', {}, 'Автор: '), book.author?.fullName || '—'));
    appV2.appendChild(h('p', {}, h('b', {}, 'Жанры: '),
        (book.genres || []).map(g => h('span', {class: 'tag'}, g.name))
    ));
    appV2.appendChild(h('p', {},
        h('a', {href: `#/books/${id}/edit`}, 'Редактировать'), ' ',
        h('button', {
            onClick: async () => {
                if (!confirm('Удалить книгу?')) return;
                await api(`/api/v1/books/${id}`, {method: 'DELETE'});
                goto('#/books');
            }
        }, 'Удалить')
    ));

    const comments = await api(`/api/v1/books/${id}/comments?page=0&size=100`);
    appV2.appendChild(h('h2', {}, 'Комментарии'));
    if (comments.totalElements === 0) {
        appV2.appendChild(h('div', {class: 'muted'}, 'Пока нет комментариев'));
    } else {
        const ul = h('ul', {},
            comments.content.map(c => h('li', {},
                h('span', {class: 'muted'}, new Date(c.createdAt).toLocaleString()), ' — ',
                h('span', {}, c.text), ' ',
                h('button', {
                    onClick: async () => {
                        if (!confirm('Удалить комментарий?')) return;
                        await api(`/api/v1/books/${id}/comments/${c.id}`, {method: 'DELETE'});
                        render();
                    }
                }, 'Удалить')
            ))
        );
        appV2.appendChild(ul);
    }

    appV2.appendChild(h('h3', {}, 'Добавить комментарий'));
    const form = h('form', {onSubmit: onSubmitComment, method: 'post', action: '#'});
    const ta = h('textarea', {name: 'text', rows: '3', cols: '60', maxlength: '2048', required: true});
    form.appendChild(ta);
    form.appendChild(h('div', {}, h('button', {type: 'submit'}, 'Добавить')));
    const errorBox = h('div');
    appV2.appendChild(errorBox);
    appV2.appendChild(form);

    async function onSubmitComment(e) {
        e.preventDefault();
        errorBox.innerHTML = '';
        const text = (ta.value ?? '').trim();
        if (!text) {
            errorBox.appendChild(showError(new Error('Текст комментария не должен быть пустым')));
            return;
        }
        try {
            await api(`/api/v1/books/${id}/comments`, {method: 'POST', body: {text}});
            ta.value = '';
            render();
        } catch (err) {
            errorBox.appendChild(showProblem(err instanceof ApiError ? err : new ApiError({detail: String(err)})));
        }
    }
});

route(/^#\/books\/new$/, async () => {
    await renderBookForm();
});

route(/^#\/books\/(\d+)\/edit$/, async (id) => {
    const book = await api(`/api/v1/books/${id}`);
    await renderBookForm(book);
});

route(/^#\/authors$/, async () => {
    const authors = await api('/api/v1/authors');
    appV2.appendChild(h('h1', {}, 'Авторы'));
    const table = h('table', {},
        h('thead', {}, h('tr', {}, h('th', {}, 'ID'), h('th', {}, 'ФИО'))),
        h('tbody', {}, authors.map(a => h('tr', {}, h('td', {}, String(a.id)), h('td', {}, a.fullName))))
    );
    appV2.appendChild(table);
});

route(/^#\/genres$/, async () => {
    const page = await api('/api/v1/genres?page=0&size=200');
    appV2.appendChild(h('h1', {}, 'Жанры'));
    const table = h('table', {},
        h('thead', {}, h('tr', {}, h('th', {}, 'ID'), h('th', {}, 'Название'))),
        h('tbody', {}, page.content.map(g => h('tr', {}, h('td', {}, String(g.id)), h('td', {}, g.name))))
    );
    appV2.appendChild(table);
});

appV2.addEventListener('submit', (e) => {
    e.preventDefault();
}, true);

async function renderBookForm(existing) {
    const isEdit = !!existing;
    const [authors, genresPage] = await Promise.all([
        api('/api/v1/authors'),
        api('/api/v1/genres?page=0&size=200')
    ]);
    const genres = genresPage.content;

    appV2.appendChild(h('h1', {}, isEdit ? 'Редактировать книгу' : 'Создать книгу'));

    const form = h('form', {onSubmit: onSubmit, method: 'post', action: '#'});
    const errorBox = h('div');

    form.appendChild(h('div', {},
        h('label', {for: 'title'}, 'Название'),
        h('br'),
        h('input', {
            id: 'title',
            name: 'title',
            type: 'text',
            maxlength: '255',
            required: true,
            value: existing?.title || ''
        }),
        h('div', {'data-error-for': 'title', class: 'muted'})
    ));

    const selAuthor = h('select', {name: 'authorId', required: true},
        h('option', {value: '', disabled: true, selected: !existing}, '— выберите автора —'),
        authors.map(a => h('option', {
            value: a.id,
            selected: !!(existing && existing.author && existing.author.id === a.id)
        }, a.fullName))
    );
    form.appendChild(h('div', {}, h('label', {}, 'Автор'), h('br'), selAuthor,h('div', { 'data-error-for': 'authorId', class: 'muted' })));

    const selGenres = h('select', {name: 'genresIds', multiple: true, size: 6, required: true},
        genres.map(g => h('option', {
            value: g.id,
            selected: !!(existing && (existing.genres || []).some(x => x.id === g.id))
        }, g.name))
    );
    form.appendChild(h('div', {}, h('label', {}, 'Жанры'), h('br'), selGenres, h('div', { 'data-error-for': 'genresIds', class: 'muted' })));

    const submitBtn = h('button', {type: 'submit'}, 'Сохранить');
    form.appendChild(h('div', {style: 'margin-top:10px'},
        submitBtn,
        ' ',
        h('a', {href: isEdit ? `#/books/${existing.id}` : '#/books'}, 'Отмена')
    ));

    appV2.appendChild(errorBox);
    appV2.appendChild(form);

    async function onSubmit(e) {
        e.preventDefault();
        errorBox.innerHTML = '';
        clearFieldErrors(form);

        const titleRaw = (form.querySelector('input[name=title]').value ?? '').trim();
        if (!titleRaw) {
            errorBox.appendChild(showError(new Error('Название не должно быть пустым')));
            return;
        }

        const authorRaw = selAuthor.value;
        if (!authorRaw) {
            errorBox.appendChild(showError(new Error('Выберите автора')));
            return;
        }
        const authorId = Number(authorRaw);

        const genresIds = Array.from(selGenres.selectedOptions).map(o => Number(o.value));
        if (genresIds.length === 0) {
            errorBox.appendChild(showError(new Error('Выберите хотя бы один жанр')));
            return;
        }

        const payload = {
            id: existing?.id ?? null,
            title: titleRaw,
            authorId,
            genresIds,
            version: existing?.version ?? 0
        };

        submitBtn.disabled = true;
        try {
            if (isEdit) {
                await api(`/api/v1/books/${existing.id}`, {method: 'PUT', body: payload});
                goto('#/books');
            } else {
                await api('/api/v1/books', {method: 'POST', body: payload});
                goto('#/books');
            }
        } catch (err) {
            const applied = applyFieldErrors(form, err);
            if (!applied) {
                errorBox.appendChild(showProblem(err instanceof ApiError ? err : new ApiError({detail: String(err)})));
            }
        } finally {
            submitBtn.disabled = false;
        }
    }
}
