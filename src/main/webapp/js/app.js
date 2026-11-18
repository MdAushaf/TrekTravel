/**
 * TrekTravel Client-side Utilities & Interactions
 * Features:
 * - data-confirm popup dialogs
 * - Live total price calculator (price * participants * (1 - discount))
 * - Real-time password confirmation check
 * - Date input min-date auto-constraining
 */

document.addEventListener("DOMContentLoaded", () => {
    // 1. data-confirm confirmation dialogs
    document.querySelectorAll("[data-confirm]").forEach(element => {
        element.addEventListener("click", event => {
            const message = element.getAttribute("data-confirm") || "Are you sure you want to proceed?";
            if (!confirm(message)) {
                event.preventDefault();
            }
        });
    });

    // 2. Live total price calculator for booking dialogs & forms
    const numParticipantsInput = document.getElementById("numParticipants");
    const liveTotalDisplay = document.getElementById("liveTotalPrice");
    const liveDiscountDisplay = document.getElementById("liveDiscountAmount");

    function updateLiveTotal() {
        if (!numParticipantsInput || !liveTotalDisplay) return;

        const count = parseInt(numParticipantsInput.value, 10) || 1;
        const basePrice = parseFloat(liveTotalDisplay.dataset.basePrice) || 0;
        const discountPercent = parseInt(liveTotalDisplay.dataset.discountPercent, 10) || 0;

        const grossTotal = basePrice * count;
        const discountAmount = grossTotal * (discountPercent / 100);
        const netTotal = grossTotal - discountAmount;

        liveTotalDisplay.textContent = "₹" + netTotal.toLocaleString("en-IN", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });

        if (liveDiscountDisplay && discountAmount > 0) {
            liveDiscountDisplay.textContent = "₹" + discountAmount.toLocaleString("en-IN", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });
        }
    }

    if (numParticipantsInput) {
        numParticipantsInput.addEventListener("input", updateLiveTotal);
        numParticipantsInput.addEventListener("change", updateLiveTotal);
        updateLiveTotal();
    }

    // 3. Password match validation on registration form
    const passwordInput = document.getElementById("regPassword");
    const confirmInput = document.getElementById("regConfirmPassword");
    const matchWarning = document.getElementById("passwordMismatchWarning");

    function checkPasswordMatch() {
        if (!passwordInput || !confirmInput || !matchWarning) return;
        if (confirmInput.value.length > 0 && passwordInput.value !== confirmInput.value) {
            matchWarning.classList.remove("d-none");
            confirmInput.classList.add("is-invalid");
        } else {
            matchWarning.classList.add("d-none");
            confirmInput.classList.remove("is-invalid");
        }
    }

    if (passwordInput && confirmInput) {
        passwordInput.addEventListener("input", checkPasswordMatch);
        confirmInput.addEventListener("input", checkPasswordMatch);
    }

    // 4. Auto-constrain future date pickers (e.g. batch start dates)
    const futureDateInputs = document.querySelectorAll("input[type='date'].future-date-only");
    if (futureDateInputs.length > 0) {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const yyyy = tomorrow.getFullYear();
        const mm = String(tomorrow.getMonth() + 1).padStart(2, "0");
        const dd = String(tomorrow.getDate()).padStart(2, "0");
        const minDateStr = `${yyyy}-${mm}-${dd}`;

        futureDateInputs.forEach(input => {
            input.setAttribute("min", minDateStr);
        });
    }
});
