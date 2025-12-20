import { $app, mount, h, showProblem } from '../core/dom.js';
import { ApiError } from '../core/api.js';

export class BaseView {
    constructor(app) { this.app = app; this.root = null; }

    async render(params) { throw new Error('override render'); }

    async show(params = []) {
        try {
            const args = Array.isArray(params) ? params : [params];
            this.root = await this.render(...args);
            mount(this.root);
        } catch (e) {
            mount(h('div', {}, showProblem(e instanceof ApiError ? e : new ApiError({ detail: String(e) }))));
        }
    }
}

