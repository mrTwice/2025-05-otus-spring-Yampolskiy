import { BaseView } from './BaseView.js';
import { h } from '../core/dom.js';

export class AuthorsView extends BaseView {
    async render() {
        const authors = await this.app.api.get('/api/v1/authors');

        return h('div', {},
            h('h1', {}, 'Авторы'),
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
