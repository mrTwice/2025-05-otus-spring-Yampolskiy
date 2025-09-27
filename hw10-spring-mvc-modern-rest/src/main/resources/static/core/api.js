export class ApiError extends Error {
    constructor({ status, title, detail, code, errors, instance, raw }) {
        super((title || 'ERROR') + (detail ? `: ${detail}` : ''));
        this.name = 'ApiError';
        this.status = status || 0;
        this.title = title || 'ERROR';
        this.detail = detail || '';
        this.code = code || null;
        this.errors = Array.isArray(errors) ? errors : [];
        this.instance = instance || null;
        this.raw = raw;
    }
}

export class ApiClient {
    constructor(base = '') { this.base = base; }

    async request(path, { method = 'GET', body, headers } = {}) {
        const url = this.base + path;
        const opts = {
            method,
            headers: { 'Content-Type': 'application/json', ...(headers || {}) },
            body: body != null ? JSON.stringify(body) : undefined
        };
        console.log('[API →]', method, url, body ?? null);

        let res;
        try {
            res = await fetch(url, opts);
        } catch (e) {
            console.error('[API ✖ NET]', e);
            throw new ApiError({ status: 0, title: 'NETWORK_ERROR', detail: e.message });
        }

        console.log('[API ←]', res.status, res.statusText, res.url);
        const ct = res.headers.get('content-type') || '';
        const isJson = ct.includes('application/json') || ct.includes('application/problem+json');
        let payload = null;
        if (res.status !== 204 && isJson) {
            try { payload = await res.json(); } catch {}
        }
        if (!res.ok) {
            const p = payload || {};
            throw new ApiError({
                status: res.status,
                title: p.title || res.statusText || `HTTP ${res.status}`,
                detail: p.detail || '',
                code: p.code || null,
                errors: p.errors || [],
                instance: p.instance || null,
                raw: p
            });
        }
        return payload;
    }

    get(p) { return this.request(p); }
    post(p, b) { return this.request(p, { method: 'POST', body: b }); }
    put(p, b) { return this.request(p, { method: 'PUT', body: b }); }
    del(p) { return this.request(p, { method: 'DELETE' }); }
}
