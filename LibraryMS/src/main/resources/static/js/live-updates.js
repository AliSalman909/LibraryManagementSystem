(() => {
    const DEFAULT_POLL_MS = 1000;
    const REPORTS_POLL_MS = 1000;
    const STUDENT_REQUESTS_POLL_MS = 1000;
    const STUDENT_FINES_POLL_MS = 1000;
    const STUDENT_RESERVATIONS_POLL_MS = 1000;
    const LIBRARIAN_RESERVATIONS_POLL_MS = 1000;
    const ALERT_MIN_VISIBLE_MS = 5000;
    let inFlight = false;
    const TABLE_WRAP_SELECTOR = ".table-wrap";

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

    function hasRecentVisibleAlert() {
        const alerts = document.querySelectorAll(".alert");
        const now = Date.now();
        for (const alertEl of alerts) {
            if (!(alertEl instanceof HTMLElement)) {
                continue;
            }
            if (alertEl.getAttribute("aria-hidden") === "true" || alertEl.style.display === "none") {
                continue;
            }
            const createdAtRaw = alertEl.getAttribute("data-alert-created-at");
            const createdAt = createdAtRaw ? Number(createdAtRaw) : NaN;
            if (Number.isFinite(createdAt) && now - createdAt < ALERT_MIN_VISIBLE_MS) {
                return true;
            }
        }
        return false;
    }

    function shouldRunOnThisPage() {
        const path = window.location.pathname || "";
        // Avoid interrupting auth forms unless user left them idle with no edits.
        // Student books has its own targeted table refresh logic.
        return !path.startsWith("/error") && path !== "/student/books";
    }

    function pollIntervalForPath() {
        const path = window.location.pathname || "";
        if (path.startsWith("/admin/reports/")) {
            return REPORTS_POLL_MS;
        }
        if (path === "/student/borrow-requests") {
            return STUDENT_REQUESTS_POLL_MS;
        }
        if (path === "/student/fines") {
            return STUDENT_FINES_POLL_MS;
        }
        if (path === "/student/reservations") {
            return STUDENT_RESERVATIONS_POLL_MS;
        }
        if (path === "/librarian/reservations") {
            return LIBRARIAN_RESERVATIONS_POLL_MS;
        }
        return DEFAULT_POLL_MS;
    }

    function buildScrollKey(el, index) {
        const explicitKey = el.getAttribute("data-scroll-key");
        if (explicitKey) return explicitKey;
        const classes = (el.className || "").trim().replace(/\s+/g, ".");
        return (classes ? "." + classes : TABLE_WRAP_SELECTOR) + "#" + index;
    }

    function captureHorizontalScrollPositions(root) {
        if (!root) return new Map();
        const positions = new Map();
        const wraps = root.querySelectorAll(TABLE_WRAP_SELECTOR);
        wraps.forEach((el, index) => {
            if (el.scrollWidth > el.clientWidth) {
                positions.set(buildScrollKey(el, index), el.scrollLeft);
            }
        });
        return positions;
    }

    function restoreHorizontalScrollPositions(root, positions) {
        if (!root || !positions || positions.size === 0) return;
        const wraps = root.querySelectorAll(TABLE_WRAP_SELECTOR);
        wraps.forEach((el, index) => {
            const key = buildScrollKey(el, index);
            const left = positions.get(key);
            if (typeof left === "number") {
                el.scrollLeft = left;
            }
        });
    }

    async function refreshFragments() {
        if (inFlight || document.hidden || !shouldRunOnThisPage()) return;
        if (hasUnsavedOrActiveInput()) return;
        if (hasRecentVisibleAlert()) return;

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
                const tableWrapScrollPositions = captureHorizontalScrollPositions(currentMain);
                currentMain.replaceWith(nextMain);
                // Restore horizontal scroll so wide tables stay where the user left them.
                requestAnimationFrame(() => restoreHorizontalScrollPositions(nextMain, tableWrapScrollPositions));
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
        window.setInterval(refreshFragments, pollIntervalForPath());
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
