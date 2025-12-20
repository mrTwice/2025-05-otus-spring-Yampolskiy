import { BaseView } from './BaseView.js';
import { h, showProblem } from '../core/dom.js';
import { ApiError } from '../core/api.js';

export class BookDetailsView extends BaseView {
    async render(id) {
        const book = await this.app.api.get(`/api/v1/books/${id}`);
        const comments = await this.app.api.get(`/api/v1/books/${id}/comments?page=0&size=100`);

        const api = this.app.api;
        const router = this.app.router;

        const root = h('div', {},
            h('h1', {}, book.title),
            h('p', {}, h('b', {}, 'Автор: '), book.author?.fullName || '—'),
            h('p', {}, h('b', {}, 'Жанры: '), (book.genres || []).map(g => h('span', { class: 'tag' }, g.name))),
            h('p', {},
                h('a', { href: `#/books/${id}/edit` }, 'Редактировать'), ' ',
                h('button', {
                    onClick: async () => {
                        if (!confirm('Удалить книгу?')) return;
                        await api.del(`/api/v1/books/${id}`);
                        router.goto('#/books');
                    }
                }, 'Удалить')
            ),
            h('h2', {}, 'Комментарии'),
            (comments.totalElements === 0)
                ? h('div', { class: 'muted' }, 'Пока нет комментариев')
                : h('ul', {},
                    comments.content.map(c => h('li', {},
                        h('span', { class: 'muted' }, new Date(c.createdAt).toLocaleString()),
                        ' — ',
                        h('span', {}, c.text), ' ',
                        h('button', {
                            onClick: async () => {
                                if (!confirm('Удалить комментарий?')) return;
                                await api.del(`/api/v1/books/${id}/comments/${c.id}`);
                                router.reload();
                            }
                        }, 'Удалить')
                    ))
                ),
            (() => {
                const form = h('form', { onSubmit: onSubmitComment, method: 'post', action: '#' });
                const ta = h('textarea', { name: 'text', rows: '3', cols: '60', maxlength: '2048', required: true });
                const errorBox = h('div');
                form.appendChild(ta);
                form.appendChild(h('div', {}, h('button', { type: 'submit' }, 'Добавить')));

                async function onSubmitComment(e) {
                    e.preventDefault();
                    errorBox.innerHTML = '';
                    const text = (ta.value ?? '').trim();
                    if (!text) {
                        errorBox.appendChild(
                            showProblem(new ApiError({ title: 'VALIDATION', detail: 'Текст комментария не должен быть пустым' }))
                        );
                        return;
                    }
                    try {
                        await api.post(`/api/v1/books/${id}/comments`, { text });
                        ta.value = '';
                        ta.focus();
                        router.reload();
                    } catch (err) {
                        errorBox.appendChild(
                            showProblem(err instanceof ApiError ? err : new ApiError({ detail: String(err) }))
                        );
                    }
                }

                return h('div', {}, h('h3', {}, 'Добавить комментарий'), errorBox, form);
            })()
        );

        return root;
    }
}
