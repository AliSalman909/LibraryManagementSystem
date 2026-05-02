(() => {
    "use strict";

    const FIELD_SELECTOR = "input, textarea, select";
    const PASSWORD_SELECTOR = "input[type='password']";
    const FORM_GUARD_ATTR = "data-no-autofill-guard";

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
        field.setAttribute("data-lpignore", "true");
        field.setAttribute("data-1p-ignore", "true");
        field.setAttribute("data-form-type", "other");
        if (field.matches("input") && field.type !== "file") {
            armReadonlyGuard(field);
        }
    }

    function addDecoyFields(form) {
        if (!form || form.getAttribute(FORM_GUARD_ATTR) === "true") return;

        const wrap = document.createElement("div");
        wrap.setAttribute("aria-hidden", "true");
        wrap.style.position = "absolute";
        wrap.style.left = "-9999px";
        wrap.style.width = "1px";
        wrap.style.height = "1px";
        wrap.style.overflow = "hidden";

        const fakeUser = document.createElement("input");
        fakeUser.type = "text";
        fakeUser.name = "fakeUsername";
        fakeUser.setAttribute("autocomplete", "username");
        fakeUser.setAttribute("tabindex", "-1");
        fakeUser.setAttribute("readonly", "readonly");

        const fakePass = document.createElement("input");
        fakePass.type = "password";
        fakePass.name = "fakePassword";
        fakePass.setAttribute("autocomplete", "new-password");
        fakePass.setAttribute("tabindex", "-1");
        fakePass.setAttribute("readonly", "readonly");

        wrap.appendChild(fakeUser);
        wrap.appendChild(fakePass);
        form.insertBefore(wrap, form.firstChild);
        form.setAttribute(FORM_GUARD_ATTR, "true");
    }

    function hardenForm(form) {
        if (!form) return;
        form.setAttribute("autocomplete", "off");
        form.setAttribute("data-lpignore", "true");
        addDecoyFields(form);
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
