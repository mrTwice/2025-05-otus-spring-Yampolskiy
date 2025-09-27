import { BaseView } from './BaseView.js';
import { h, showProblem } from '../core/dom.js';

export class AuthorsView extends BaseView {
    async render() {
        const api = this.app.api;
        const router = this.app.router;

        const authors = await api.get('/api/v1/authors');

        const errorBox = h('div');
        const input = h('input', { type: 'text', name: 'fullName', maxlength: '255', required: true, autofocus: true });
        const submitBtn = h('button', { type: 'submit' }, 'Добавить');
        const form = h('form', {
                method: 'post',
                action: '#',
                onSubmit: async (e) => {
                    e.preventDefault();
                    errorBox.innerHTML = '';
                    const fullName = (input.value ?? '').trim();
                    if (!fullName) {
                        errorBox.appendChild(showProblem({ title: 'VALIDATION', detail: 'ФИО не должно быть пустым' }));
                        return;
                    }
                    submitBtn.disabled = true;
                    try {
                        await api.post('/api/v1/authors', { fullName });
                        router.reload();
                    } catch (err) {
                        errorBox.appendChild(showProblem(err));
                    } finally {
                        submitBtn.disabled = false;
                    }
                }
            },
            h('div', {}, h('label', { for: 'fullName' }, 'ФИО')),
            input,
            h('div', { style: 'margin-top:8px' }, submitBtn)
        );

        return h('div', {},
            h('h1', {}, 'Авторы'),
            h('h3', {}, 'Добавить автора'),
            errorBox,
            form,
            h('hr'),
            h('table', {},
                h('thead', {}, h('tr', {},
                    h('th', {}, 'ID'), h('th', {}, 'ФИО')
                )),
                h('tbody', {},
                    authors.map(a => h('tr', {},
                        h('td', {}, String(a.id)),
                        h('td', {}, a.fullName)
                    ))
                )
            )
        );
    }
}
