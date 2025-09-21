import { BaseView } from './BaseView.js';
import { h } from '../core/dom.js';

export class GenresView extends BaseView {
    async render() {
        // читаем page/size из hash-параметров
        const qs = location.hash.split('?')[1] || '';
        const params = new URLSearchParams(qs);
        const page = Number(params.get('page') || 0);
        const size = Number(params.get('size') || 20);

        const data = await this.app.api.get(`/api/v1/genres?page=${page}&size=${size}`);

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
            table,
            pager
        );
    }
}
