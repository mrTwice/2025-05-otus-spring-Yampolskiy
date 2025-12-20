import { BaseView } from './BaseView.js';
import { h } from '../core/dom.js';

export class BooksListView extends BaseView {
    async render() {
        const page = Number(new URLSearchParams(location.hash.split('?')[1] || '').get('page') || 0);
        const size = Number(new URLSearchParams(location.hash.split('?')[1] || '').get('size') || 20);

        const pageData = await this.app.api.get(`/api/v1/books?page=${page}&size=${size}`);

        const root = h('div', {},
            h('h1', {}, 'Список книг'),
            h('p', {}, h('a', { href: '#/books/new' }, 'Добавить книгу')),
            h('table', {},
                h('thead', {}, h('tr', {},
                    h('th', {}, 'ID'), h('th', {}, 'Название'), h('th', {}, 'Автор'), h('th', {}, 'Жанры'), h('th', {}, 'Действия')
                )),
                h('tbody', {},
                    pageData.content.map(b => h('tr', {},
                        h('td', {}, String(b.id)),
                        h('td', {}, h('a', { href: `#/books/${b.id}` }, b.title)),
                        h('td', {}, b.authorFullName ?? ''),
                        h('td', {}, b.genresSummary ?? ''),
                        h('td', {},
                            h('a', { href: `#/books/${b.id}/edit` }, 'Редактировать'), ' ',
                            h('button', {
                                onClick: async () => {
                                    if (!confirm('Удалить книгу?')) return;
                                    await this.app.api.del(`/api/v1/books/${b.id}`);
                                    this.app.router.reload();
                                }
                            }, 'Удалить')
                        )
                    ))
                )
            ),
            (() => {
                const p = h('p', { class: 'muted' },
                    `Всего: ${pageData.totalElements} • Стр. ${pageData.totalPages === 0 ? 0 : pageData.page + 1} из ${pageData.totalPages}`
                );
                if (!pageData.first) p.appendChild(h('a', { href: `#/books?page=${pageData.page - 1}&size=${size}`, style: 'margin-left:8px' }, '← Пред'));
                if (!pageData.last)  p.appendChild(h('a', { href: `#/books?page=${pageData.page + 1}&size=${size}`, style: 'margin-left:8px' }, 'След →'));
                return p;
            })()
        );
        return root;
    }
}
