import {ApiClient} from './core/api.js';
import {mount, h} from './core/dom.js';
import {Router} from './core/router.js';

import {BooksListView} from './views/BooksListView.js';
import {BookDetailsView} from './views/BookDetailsView.js';
import {BookFormView} from './views/BookFormView.js';
import {AuthorsView} from './views/AuthorsView.js';
import {GenresView} from './views/GenresView.js';

class App {
    constructor() {
        this.api = new ApiClient('');
        this.router = new Router({ defaultHash: '#/books' });

        this.router
            .add(/^#\/books(?:\?.*)?$/,        () => new BooksListView(this).show())

            .add(/^#\/books\/new$/,            () => new BookFormView(this).show(null))

            .add(/^#\/books\/([^/]+)\/edit$/,  id => new BookFormView(this).show(id))

            .add(/^#\/books\/([^/]+)$/,        id => new BookDetailsView(this).show(id))

            .add(/^#\/authors$/,               () => new AuthorsView(this).show())
            .add(/^#\/genres(?:\?.*)?$/,       () => new GenresView(this).show())
            .setNotFound((badHash) => {
                document.title = '404 — Library';
                const root = h('div', {},
                    h('h1', { tabindex: '-1' }, '404 — Страница не найдена'),
                    h('p', {}, `Маршрут: ${badHash}`),
                    h('p', {}, h('a', { href: '#/books' }, 'Перейти к списку книг'))
                );
                mount(root);
                root.querySelector('h1')?.focus();
            })
            .setBeforeEach(async (hash) => {
                document.body.classList.add('loading');
                console.log('Переход к', hash);
            })
            .setAfterEach(async (hash) => {
                document.body.classList.remove('loading');
                console.log('Маршрут отрисован', hash);
            });
    }

    start() {
        window.app = this;
        if (!location.hash || location.hash === '#') {
            this.router.goto('#/books', { force: true });
        }

        this.router.start();
    }
}

new App().start();
