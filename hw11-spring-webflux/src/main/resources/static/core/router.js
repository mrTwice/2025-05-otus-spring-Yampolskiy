export class Router {

    constructor(opts = {}) {
        this.routes = [];
        this._notFound = null;
        this._beforeEach = null;
        this._afterEach = null;

        this.defaultHash = opts.defaultHash ?? '#/';
        this.onChange = this.onChange.bind(this);

        this._token = 0;
        this._lastHashProcessed = null;
    }

    add(re, handler) { this.routes.push({ re, handler }); return this; }

    setNotFound(handler) { this._notFound = handler; return this; }

    setBeforeEach(fn) { this._beforeEach = fn; return this; }
    setAfterEach(fn)  { this._afterEach  = fn; return this; }

    start() {
        window.addEventListener('hashchange', this.onChange);
        window.addEventListener('DOMContentLoaded', this.onChange);

        if (document.readyState === 'interactive' || document.readyState === 'complete') {
            Promise.resolve().then(this.onChange);
        }
    }

    stop() {
        window.removeEventListener('hashchange', this.onChange);
        window.removeEventListener('DOMContentLoaded', this.onChange);
    }

    goto(hash, { force = false } = {}) {
        if (!hash.startsWith('#')) hash = '#' + hash;

        if (location.hash === hash) {
            if (force) {
                this._lastHashProcessed = null;
                this.onChange();
            }
            return;
        }
        location.hash = hash;
    }

    reload() {
        this._lastHashProcessed = null;
        this.onChange();
    }

    _splitHash(hash) {
        if (!hash || hash === '#') hash = this.defaultHash;
        if (!hash.startsWith('#')) hash = '#' + hash;

        const qIdx = hash.indexOf('?');
        const path = qIdx === -1 ? hash : hash.slice(0, qIdx);
        const query = qIdx === -1 ? '' : hash.slice(qIdx + 1);
        return { full: hash, path, query };
    }

    async onChange() {
        const my = ++this._token;

        const raw = location.hash || this.defaultHash;
        const { full, path } = this._splitHash(raw);

        if (full === this._lastHashProcessed) return;

        let beforeAddedLoading = false;

        try {
            if (this._beforeEach) {
                await this._beforeEach(full);
                beforeAddedLoading = true;
                if (my !== this._token) return;
            }

            for (const r of this.routes) {
                const m = path.match(r.re);
                if (!m) continue;

                const params = m.slice(1).map(p => {
                    try { return decodeURIComponent(p); } catch { return p; }
                });

                await r.handler(...params);
                if (my !== this._token) return;


                this._lastHashProcessed = full;

                if (this._afterEach) await this._afterEach(full);
                if (my !== this._token) return;
                return;
            }

            if (this._notFound) {
                await this._notFound(full);
                if (my !== this._token) return;
                this._lastHashProcessed = full;
                if (this._afterEach) await this._afterEach(full);
                return;
            }

            this.goto(this.defaultHash);

        } catch (e) {
            console.error('Router error:', e);
            if (this._notFound) {
                try {
                    await this._notFound(full);
                    this._lastHashProcessed = full;
                } catch (e2) {
                    console.error('Router notFound failed:', e2);
                }
            } else {
                this.goto(this.defaultHash);
            }
        } finally {
            if (beforeAddedLoading && document.body.classList.contains('loading')) {
                document.body.classList.remove('loading');
            }
        }
    }

}
