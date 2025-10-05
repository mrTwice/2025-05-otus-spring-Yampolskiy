import { BaseView } from './BaseView.js';
import { h, showProblem, clearFieldErrors, applyFieldErrors } from '../core/dom.js';
import { ApiError } from '../core/api.js';

export class BookFormView extends BaseView {
    async render(existingIdOrNull) {
        const isEdit = !!existingIdOrNull;
        const [authors, genresPage, existing] = await Promise.all([
            this.app.api.get('/api/v1/authors'),
            this.app.api.get('/api/v1/genres?page=0&size=200'),
            isEdit ? this.app.api.get(`/api/v1/books/${existingIdOrNull}`) : Promise.resolve(null)
        ]);
        const genres = genresPage.content;

        const form = h('form', { onSubmit: onSubmit, method: 'post', action: '#' });
        const errorBox = h('div');
        const root = h('div', {},
            h('h1', {}, isEdit ? 'Редактировать книгу' : 'Создать книгу'),
            errorBox,
            form
        );

        form.appendChild(h('div', {},
            h('label', { for: 'title' }, 'Название'), h('br'),
            h('input', { id:'title', name:'title', type:'text', maxlength:'255', required:true, value: existing?.title || '' }),
            h('div', { 'data-error-for':'title', class:'muted' })
        ));
        const selAuthor = h('select', { name:'authorId', required:true },
            h('option', { value:'', disabled:true, selected:!isEdit }, '— выберите автора —'),
            authors.map(a => h('option', { value:a.id, selected: !!(existing && existing.author?.id === a.id) }, a.fullName))
        );
        form.appendChild(h('div', {}, h('label', {}, 'Автор'), h('br'), selAuthor, h('div', { 'data-error-for':'authorId', class:'muted' })));
        const selGenres = h('select', { name:'genresIds', multiple:true, size:6, required:true },
            genres.map(g => h('option', { value:g.id, selected: !!(existing && (existing.genres||[]).some(x => x.id === g.id)) }, g.name))
        );
        form.appendChild(h('div', {}, h('label', {}, 'Жанры'), h('br'), selGenres, h('div', { 'data-error-for':'genresIds', class:'muted' })));

        const submitBtn = h('button', { type:'submit' }, 'Сохранить');
        form.appendChild(h('div', { style:'margin-top:10px' },
            submitBtn, ' ', h('a', { href: isEdit ? `#/books/${existing.id}` : '#/books' }, 'Отмена')
        ));

        async function onSubmit(e) {
            e.preventDefault();
            errorBox.innerHTML = ''; clearFieldErrors(form);

            const titleRaw = (form.querySelector('input[name=title]').value ?? '').trim();
            if (!titleRaw) { errorBox.appendChild(showProblem(new ApiError({ title:'VALIDATION', detail:'Название не должно быть пустым' }))); return; }
            const authorRaw = selAuthor.value; if (!authorRaw) { errorBox.appendChild(showProblem(new ApiError({ title:'VALIDATION', detail:'Выберите автора' }))); return; }
            const authorId = selAuthor.value;
            const genresIds = Array.from(selGenres.selectedOptions).map(o => o.value);

            if (genresIds.length === 0) { errorBox.appendChild(showProblem(new ApiError({ title:'VALIDATION', detail:'Выберите хотя бы один жанр' }))); return; }

            const payload = { id: existing?.id ?? null, title: titleRaw, authorId, genresIds, version: existing?.version ?? 0 };

            submitBtn.disabled = true;
            try {
                if (isEdit) await window.app.api.put(`/api/v1/books/${existing.id}`, payload);
                else        await window.app.api.post('/api/v1/books', payload);
                window.app.router.goto('#/books');
            } catch (err) {
                const applied = applyFieldErrors(form, err);
                if (!applied) errorBox.appendChild(showProblem(err instanceof ApiError ? err : new ApiError({ detail: String(err) })));
            } finally {
                submitBtn.disabled = false;
            }
        }

        return root;
    }
}
