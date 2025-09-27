export const $app = document.getElementById('app');

export function h(tag, attrs = {}, ...children) {
    const el = document.createElement(tag);
    for (const [k, v] of Object.entries(attrs || {})) {
        if (k === 'class') el.className = v;
        else if (k.startsWith('on') && typeof v === 'function') el.addEventListener(k.slice(2).toLowerCase(), v);
        else if (v != null) el.setAttribute(k, v);
    }
    for (const c of children.flat()) {
        if (c == null) continue;
        el.appendChild(typeof c === 'string' ? document.createTextNode(c) : c);
    }
    return el;
}

export function mount(root) {
    $app.innerHTML = '';
    if (root) $app.appendChild(root);
}

export function showProblem(err) {
    const box = h('div', { class: 'error' });
    const header = `${err.title || 'ERROR'}${err.status ? ` (${err.status})` : ''}`;
    box.appendChild(h('div', {}, header));
    if (err.detail) box.appendChild(h('div', {}, err.detail));
    if (Array.isArray(err.errors) && err.errors.length) {
        const ul = h('ul');
        err.errors.forEach(e => ul.appendChild(h('li', {}, `${e.field}: ${e.message}`)));
        box.appendChild(ul);
    }
    return box;
}

export function clearFieldErrors(form) {
    form.querySelectorAll('[data-error-for]').forEach(n => n.textContent = '');
}
export function applyFieldErrors(form, err) {
    if (!err || !Array.isArray(err.errors)) return false;
    let ok = false;
    err.errors.forEach(({ field, message }) => {
        const n = form.querySelector(`[data-error-for="${field}"]`);
        if (n) { n.textContent = message; ok = true; }
    });
    return ok;
}
