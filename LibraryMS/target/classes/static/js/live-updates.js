(() => {
    const POLL_MS = 5000;
    let inFlight = false;

    function isEditableElement(el) {
        if (!el) return false;
        const tag = el.tagName;
        return (
            tag === "INPUT" ||
            tag === "TEXTAREA" ||
            tag === "SELECT" ||
            el.isContentEditable
        );
    }

    function isElementDirty(el) {
        if (!el || el.disabled || el.readOnly) return false;
        const tag = el.tagName;
        const type = (el.type || "").toLowerCase();

        if (tag === "INPUT") {
            if (type === "checkbox" || type === "radio") {
                return el.checked !== el.defaultChecked;
            }
            if (type === "hidden" || type === "submit" || type === "button") {
                return false;
            }
            return el.value !== el.defaultValue;
        }

        if (tag === "TEXTAREA") {
            return el.value !== el.defaultValue;
        }

        if (tag === "SELECT") {
            return Array.from(el.options).some((opt) => opt.selected !== opt.defaultSelected);
        }

        if (el.isContentEditable) {
            return true;
        }

        return false;
    }

    function hasUnsavedOrActiveInput() {
        const active = document.activeElement;
        if (isEditableElement(active)) return true;

        const editable = document.querySelectorAll("input, textarea, select, [contenteditable='true']");
        for (const el of editable) {
            if (isElementDirty(el)) return true;
        }
        return false;
    }

    function shouldRunOnThisPage() {
        const path = window.location.pathname || "";
        // Avoid interrupting auth forms unless user left them idle with no edits.
        return !path.startsWith("/error");
    }

    async function refreshFragments() {
        if (inFlight || document.hidden || !shouldRunOnThisPage()) return;
        if (hasUnsavedOrActiveInput()) return;

        inFlight = true;
        try {
            const res = await fetch(window.location.href, {
                method: "GET",
                credentials: "same-origin",
                cache: "no-store",
                headers: {
                    "X-Requested-With": "live-updates",
                },
            });
            if (!res.ok) return;

            const html = await res.text();
            const parser = new DOMParser();
            const nextDoc = parser.parseFromString(html, "text/html");

            const currentMain = document.querySelector("main");
            const nextMain = nextDoc.querySelector("main");
            if (currentMain && nextMain && currentMain.innerHTML !== nextMain.innerHTML) {
                currentMain.replaceWith(nextMain);
            }

            const currentHeader = document.querySelector(".site-header");
            const nextHeader = nextDoc.querySelector(".site-header");
            if (currentHeader && nextHeader && currentHeader.innerHTML !== nextHeader.innerHTML) {
                currentHeader.replaceWith(nextHeader);
            }

            const currentFooter = document.querySelector(".site-footer");
            const nextFooter = nextDoc.querySelector(".site-footer");
            if (currentFooter && nextFooter && currentFooter.innerHTML !== nextFooter.innerHTML) {
                currentFooter.replaceWith(nextFooter);
            }

            if (nextDoc.title && document.title !== nextDoc.title) {
                document.title = nextDoc.title;
            }
        } catch (_) {
            // Ignore transient polling errors; next cycle will retry.
        } finally {
            inFlight = false;
        }
    }

    function start() {
        window.setInterval(refreshFragments, POLL_MS);
        document.addEventListener("visibilitychange", () => {
            if (!document.hidden) {
                refreshFragments();
            }
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", start);
    } else {
        start();
    }
})();
