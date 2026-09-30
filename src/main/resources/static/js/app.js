document.addEventListener("DOMContentLoaded", function () {

    // 1. Auto-dismiss du message de succès
    var success = document.querySelector(".alert-success");
    if (success) {
        setTimeout(function () {
            success.style.transition = "opacity 0.4s, transform 0.4s";
            success.style.opacity = "0";
            success.style.transform = "translateY(-10px)";
            setTimeout(function () { success.remove(); }, 400);
        }, 3500);
    }

    // 2. Effet ripple sur tous les boutons
    document.querySelectorAll(".btn, button").forEach(function (btn) {
        btn.addEventListener("click", function (e) {
            var rect = btn.getBoundingClientRect();
            var ripple = document.createElement("span");
            var size = Math.max(rect.width, rect.height);
            ripple.className = "ripple";
            ripple.style.width = ripple.style.height = size + "px";
            ripple.style.left = (e.clientX - rect.left - size / 2) + "px";
            ripple.style.top = (e.clientY - rect.top - size / 2) + "px";
            btn.appendChild(ripple);
            setTimeout(function () { ripple.remove(); }, 550);
        });
    });

    // 3. Apparition en cascade des lignes de tableau
    document.querySelectorAll("tbody tr").forEach(function (row, i) {
        row.style.animationDelay = (i * 0.04) + "s";
        row.classList.add("row-in");
    });

    // 4. Compteurs animés (dashboard)
    document.querySelectorAll("[data-count-target]").forEach(function (el) {
        var target = parseFloat(el.getAttribute("data-count-target"));
        var isDecimal = target % 1 !== 0;
        var duration = 900;
        var start = null;
        function step(ts) {
            if (!start) start = ts;
            var progress = Math.min((ts - start) / duration, 1);
            var eased = 1 - Math.pow(1 - progress, 3);
            var value = target * eased;
            el.textContent = isDecimal ? value.toFixed(1) + "%" : Math.round(value);
            if (progress < 1) requestAnimationFrame(step);
        }
        requestAnimationFrame(step);
    });

    // 5. Barres de progression animées (dashboard)
    setTimeout(function () {
        document.querySelectorAll("[data-target-width]").forEach(function (bar) {
            bar.style.width = bar.getAttribute("data-target-width") + "%";
        });
    }, 100);

    // 6. Lien de navigation actif
    var path = window.location.pathname;
    document.querySelectorAll(".nav-link").forEach(function (link) {
        var href = link.getAttribute("href");
        if (href && path.indexOf(href) === 0 && href !== "/") {
            link.classList.add("active");
        }
    });
});
