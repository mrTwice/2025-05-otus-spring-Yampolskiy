import { BaseView } from './BaseView.js';
import { h, showProblem } from '../core/dom.js';

export class GenresView extends BaseView {
    async render() {
        const api = this.app.api;
        const router = this.app.router;

        const qs = location.hash.split('?')[1] || '';
        const params = new URLSearchParams(qs);
        const page = Number(params.get('page') || 0);
        const size = Number(params.get('size') || 20);

        const data = await api.get(`/api/v1/genres?page=${page}&size=${size}`);

        const errorBox = h('div');
        const input = h('input', { type: 'text', name: 'name', maxlength: '255', required: true, autofocus: true });
        const submitBtn = h('button', { type: 'submit' }, 'Добавить');
        const form = h('form', {
                method: 'post',
                action: '#',
                onSubmit: async (e) => {
                    e.preventDefault();
                    errorBox.innerHTML = '';
                    const name = (input.value ?? '').trim();
                    if (!name) {
                        errorBox.appendChild(showProblem({ title: 'VALIDATION', detail: 'Название не должно быть пустым' }));
                        return;
                    }
                    submitBtn.disabled = true;
                    try {
                        await api.post('/api/v1/genres', { name });
                        router.reload();
                    } catch (err) {
                        errorBox.appendChild(showProblem(err));
                    } finally {
                        submitBtn.disabled = false;
                    }
                }
            },
            h('div', {}, h('label', { for: 'name' }, 'Название')),
            input,
            h('div', { style: 'margin-top:8px' }, submitBtn)
        );

        const table = h('table', {},
            h('thead', {}, h('tr', {},
                h('th', {}, 'ID'), h('th', {}, 'Название')
            )),
            h('tbody', {},
                data.content.map(g => h('tr', {},
                    h('td', {}, String(g.id)),
                    h('td', {}, g.name)
                ))
            )
        );

        const pager = h('p', { class: 'muted' },
            `Всего: ${data.totalElements} • Стр. ${data.totalPages === 0 ? 0 : data.page + 1} из ${data.totalPages}`
        );
        if (!data.first) {
            pager.appendChild(
                h('a', { href: `#/genres?page=${data.page - 1}&size=${size}`, style: 'margin-left:8px' }, '← Пред')
            );
        }
        if (!data.last) {
            pager.appendChild(
                h('a', { href: `#/genres?page=${data.page + 1}&size=${size}`, style: 'margin-left:8px' }, 'След →')
            );
        }

        return h('div', {},
            h('h1', {}, 'Жанры'),
            h('h3', {}, 'Добавить жанр'),
            errorBox,
            form,
            h('hr'),
            table,
            pager
        );
    }
}
