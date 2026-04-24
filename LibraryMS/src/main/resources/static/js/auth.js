(function () {
    "use strict";

    function togglePasswordForButton(btn) {
        var id = btn.getAttribute("data-toggle-target");
        var input = id && document.getElementById(id);
        if (!input) return;
        var show = input.getAttribute("type") === "password";
        input.setAttribute("type", show ? "text" : "password");
        btn.textContent = show ? "Hide" : "Show";
        btn.setAttribute("aria-label", show ? "Hide password" : "Show password");
    }

    document.querySelectorAll(".toggle-password").forEach(function (btn) {
        btn.addEventListener("click", function () {
            togglePasswordForButton(btn);
        });
    });

    // Delegated fallback for pages where JS may initialize before dynamic DOM updates.
    document.addEventListener("click", function (e) {
        var btn = e.target && e.target.closest ? e.target.closest(".toggle-password") : null;
        if (!btn) return;
        e.preventDefault();
        togglePasswordForButton(btn);
    });

    function rolePanelsSync() {
        var select = document.getElementById("role");
        if (!select) return;
        var role = select.value || "";
        document.querySelectorAll("[data-role-panels]").forEach(function (panel) {
            var roles = (panel.getAttribute("data-role-panels") || "").split(/\s+/).filter(Boolean);
            var show = roles.indexOf(role) !== -1;
            panel.hidden = !show;
            panel.querySelectorAll("input, select, textarea").forEach(function (el) {
                if (el.id === "role") return;
                if (show) {
                    el.removeAttribute("disabled");
                } else {
                    el.setAttribute("disabled", "disabled");
                }
            });
        });
    }

    var roleSelect = document.getElementById("role");
    if (roleSelect) {
        roleSelect.addEventListener("change", rolePanelsSync);
        rolePanelsSync();
    }

    function readMaxProfileBytes(form) {
        if (!form) return 10485760;
        var raw = form.getAttribute("data-max-profile-bytes");
        var n = raw ? parseInt(raw, 10) : NaN;
        return !isNaN(n) && n > 0 ? n : 10485760;
    }

    function formatMbFromBytes(bytes) {
        return Math.max(1, Math.round(bytes / (1024 * 1024))) + " MB";
    }

    var regForm = document.getElementById("register-form");
    if (regForm) {
        regForm.addEventListener("submit", function (e) {
            var pwd = document.getElementById("password");
            var confirm = document.getElementById("confirmPassword");
            if (pwd && confirm && pwd.value !== confirm.value) {
                e.preventDefault();
                confirm.setCustomValidity("The two passwords do not match.");
                confirm.reportValidity();
                return;
            }
            if (confirm) confirm.setCustomValidity("");
            var r = roleSelect && roleSelect.value;
            if (r === "STUDENT") {
                var prog = document.getElementById("program");
                if (prog && !prog.value.trim()) {
                    e.preventDefault();
                    prog.setCustomValidity("Please enter your study program.");
                    prog.reportValidity();
                    return;
                }
                if (prog) prog.setCustomValidity("");
            }
            var picInput = document.getElementById("profilePictureFile");
            if (picInput && picInput.files && picInput.files[0]) {
                var f = picInput.files[0];
                var maxB = readMaxProfileBytes(regForm);
                if (f.size > maxB) {
                    e.preventDefault();
                    showProfileFileTooLarge(maxB);
                    picInput.focus();
                }
            }
        });
    }

    var profileInput = document.getElementById("profilePictureFile");
    var profilePreview = document.getElementById("profilePicturePreview");
    var profilePreviewImg = profilePreview && profilePreview.querySelector("img");
    var profilePanHint = document.getElementById("profilePicturePanHint");
    var profileSizeError = document.getElementById("profilePictureSizeError");
    var focalXInput = document.getElementById("profilePictureFocalX");
    var focalYInput = document.getElementById("profilePictureFocalY");
    var profilePreviewUrl = null;
    var focalX = 50;
    var focalY = 50;
    var panDragging = false;
    var panLastX = 0;
    var panLastY = 0;
    var PAN_SENS = 0.22;

    function clampFocal(v) {
        return Math.min(100, Math.max(0, v));
    }

    function applyProfileFocal() {
        focalX = clampFocal(focalX);
        focalY = clampFocal(focalY);
        if (profilePreviewImg) {
            profilePreviewImg.style.objectPosition = focalX + "% " + focalY + "%";
        }
        if (focalXInput) focalXInput.value = focalX.toFixed(2);
        if (focalYInput) focalYInput.value = focalY.toFixed(2);
    }

    function resetProfileFocal() {
        focalX = 50;
        focalY = 50;
        applyProfileFocal();
    }

    function clearProfileSizeError() {
        if (profileSizeError) {
            profileSizeError.hidden = true;
            profileSizeError.textContent = "";
        }
    }

    function showProfileFileTooLarge(maxBytes) {
        if (profileSizeError) {
            profileSizeError.textContent =
                "That photo is too large. Please use an image under "
                + formatMbFromBytes(maxBytes)
                + ".";
            profileSizeError.hidden = false;
        }
        if (profileInput) profileInput.value = "";
        if (profilePreviewUrl) {
            URL.revokeObjectURL(profilePreviewUrl);
            profilePreviewUrl = null;
        }
        if (profilePreview) profilePreview.hidden = true;
        if (profilePanHint) profilePanHint.hidden = true;
        if (profilePreviewImg) profilePreviewImg.removeAttribute("src");
        resetProfileFocal();
    }

    if (profileInput && profilePreview && profilePreviewImg) {
        profileInput.addEventListener("change", function () {
            clearProfileSizeError();
            if (profilePreviewUrl) {
                URL.revokeObjectURL(profilePreviewUrl);
                profilePreviewUrl = null;
            }
            var file = profileInput.files && profileInput.files[0];
            if (!file || !file.type || file.type.indexOf("image/") !== 0) {
                profilePreview.hidden = true;
                if (profilePanHint) profilePanHint.hidden = true;
                profilePreviewImg.removeAttribute("src");
                resetProfileFocal();
                return;
            }
            var maxB = readMaxProfileBytes(regForm);
            if (file.size > maxB) {
                showProfileFileTooLarge(maxB);
                return;
            }
            profilePreviewUrl = URL.createObjectURL(file);
            profilePreviewImg.src = profilePreviewUrl;
            profilePreview.hidden = false;
            if (profilePanHint) profilePanHint.hidden = false;
            resetProfileFocal();
        });

        profilePreview.addEventListener("pointerdown", function (e) {
            if (profilePreview.hidden || e.button !== 0) return;
            panDragging = true;
            panLastX = e.clientX;
            panLastY = e.clientY;
            profilePreview.classList.add("is-dragging");
            try {
                profilePreview.setPointerCapture(e.pointerId);
            } catch (err) {
                /* ignore */
            }
        });

        profilePreview.addEventListener("pointermove", function (e) {
            if (!panDragging) return;
            var dx = e.clientX - panLastX;
            var dy = e.clientY - panLastY;
            panLastX = e.clientX;
            panLastY = e.clientY;
            focalX = clampFocal(focalX + dx * PAN_SENS);
            focalY = clampFocal(focalY + dy * PAN_SENS);
            applyProfileFocal();
            e.preventDefault();
        });

        function endPan(e) {
            if (!panDragging) return;
            panDragging = false;
            profilePreview.classList.remove("is-dragging");
            if (e && e.pointerId != null) {
                try {
                    profilePreview.releasePointerCapture(e.pointerId);
                } catch (err) {
                    /* ignore */
                }
            }
        }

        profilePreview.addEventListener("pointerup", endPan);
        profilePreview.addEventListener("pointercancel", endPan);
        profilePreview.addEventListener("lostpointercapture", function () {
            panDragging = false;
            profilePreview.classList.remove("is-dragging");
        });
    }
})();
