(() => {
    "use strict";

    const FIELD_SELECTOR = "input, textarea, select";
    const PASSWORD_SELECTOR = "input[type='password']";

    function armReadonlyGuard(field) {
        if (!field || field.dataset.noAutofillArmed === "true") return;
        field.setAttribute("readonly", "readonly");
        const unlock = () => field.removeAttribute("readonly");
        field.addEventListener("focus", unlock, { once: true });
        field.addEventListener("pointerdown", unlock, { once: true });
        field.addEventListener("keydown", unlock, { once: true });
        field.dataset.noAutofillArmed = "true";
    }

    function hardenField(field) {
        if (!field || field.disabled) return;
        field.setAttribute("autocomplete", "off");
        field.setAttribute("autocorrect", "off");
        field.setAttribute("autocapitalize", "off");
        field.setAttribute("spellcheck", "false");
        if (field.matches("input")) {
            armReadonlyGuard(field);
        }
    }

    function hardenForm(form) {
        if (!form) return;
        form.setAttribute("autocomplete", "off");
        form.querySelectorAll(FIELD_SELECTOR).forEach(hardenField);
    }

    function applyNoAutofill(root) {
        const scope = root || document;
        scope.querySelectorAll("form").forEach(hardenForm);
        scope.querySelectorAll(FIELD_SELECTOR).forEach(hardenField);
        scope.querySelectorAll(PASSWORD_SELECTOR).forEach((field) => {
            field.setAttribute("autocomplete", "new-password");
        });
    }

    function startObserver() {
        const observer = new MutationObserver((mutations) => {
            for (const mutation of mutations) {
                mutation.addedNodes.forEach((node) => {
                    if (!(node instanceof HTMLElement)) return;
                    applyNoAutofill(node);
                });
            }
        });
        observer.observe(document.documentElement, {
            childList: true,
            subtree: true,
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", () => {
            applyNoAutofill(document);
            startObserver();
        });
    } else {
        applyNoAutofill(document);
        startObserver();
    }
})();
