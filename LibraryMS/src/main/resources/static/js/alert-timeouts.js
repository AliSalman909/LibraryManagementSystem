(() => {
    const ALERT_TIMEOUT_MS = 5000;
    const SCHEDULED_ATTR = "data-alert-timeout-scheduled";
    const CREATED_AT_ATTR = "data-alert-created-at";

    function hideAlert(alertEl) {
        if (!alertEl || !alertEl.isConnected) {
            return;
        }
        alertEl.style.display = "none";
        alertEl.setAttribute("aria-hidden", "true");
    }

    function scheduleAlertTimeout(alertEl) {
        if (!alertEl || alertEl.getAttribute(SCHEDULED_ATTR) === "true") {
            return;
        }
        if (!alertEl.getAttribute(CREATED_AT_ATTR)) {
            alertEl.setAttribute(CREATED_AT_ATTR, String(Date.now()));
        }
        alertEl.setAttribute(SCHEDULED_ATTR, "true");
        window.setTimeout(() => hideAlert(alertEl), ALERT_TIMEOUT_MS);
    }

    function scheduleExistingAlerts(root = document) {
        const alerts = root.querySelectorAll(".alert");
        alerts.forEach((alertEl) => scheduleAlertTimeout(alertEl));
    }

    function observeForNewAlerts() {
        if (!document.body) {
            return;
        }
        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                mutation.addedNodes.forEach((node) => {
                    if (!(node instanceof Element)) {
                        return;
                    }
                    if (node.matches(".alert")) {
                        scheduleAlertTimeout(node);
                    }
                    scheduleExistingAlerts(node);
                });
            });
        });
        observer.observe(document.body, { childList: true, subtree: true });
    }

    function start() {
        scheduleExistingAlerts();
        observeForNewAlerts();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", start);
    } else {
        start();
    }
})();
