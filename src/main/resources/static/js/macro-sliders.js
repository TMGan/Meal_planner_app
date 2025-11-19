// ==========================================
// MACRO SLIDER CONTROLLER FOR CREATE PLAN
// ==========================================

function currentGoalValue() {
    const checked = document.querySelector('input[name="fitnessGoal"]:checked');
    return checked ? checked.value : '';
}

function disableMacroSliders() {
    const proteinEl = document.getElementById('proteinPercent');
    const carbsEl = document.getElementById('carbsPercent');
    const fatEl = document.getElementById('fatPercent');
    [proteinEl, carbsEl, fatEl].forEach(el => {
        if (el) {
            el.disabled = true;
            el.value = 0;
        }
    });
    updateMacroSplit();
}

function enableMacroSliders() {
    const proteinEl = document.getElementById('proteinPercent');
    const carbsEl = document.getElementById('carbsPercent');
    const fatEl = document.getElementById('fatPercent');
    [proteinEl, carbsEl, fatEl].forEach(el => {
        if (el) {
            el.disabled = false;
        }
    });
}

function updateRecommendedBanner() {
    const g = (currentGoalValue() || '').toLowerCase();
    const alertEl = document.getElementById('recommendedAlert');
    if (!alertEl) return;
    // Hide when no goal selected
    if (!g) {
        alertEl.style.display = 'none';
        return;
    }
    // Compute text based on goal
    let recText = '30% Protein, 40% Carbs, 30% Fat';
    let goalText = 'your goal';
    if (g.includes('lose')) {
        recText = '35% Protein, 35% Carbs, 30% Fat';
        goalText = 'losing weight';
    } else if (g.includes('build')) {
        recText = '30% Protein, 40% Carbs, 30% Fat';
        goalText = 'building muscle';
    } else if (g.includes('maintain')) {
        recText = '30% Protein, 40% Carbs, 30% Fat';
        goalText = 'maintaining weight';
    }
    const rg = document.getElementById('recommendedGoal'); if (rg) rg.textContent = goalText;
    const rs = document.getElementById('recommendedSplit'); if (rs) rs.textContent = recText;
    // Show after updating
    alertEl.style.display = 'block';
}

function updateMacroSplit() {
    const protein = parseInt(document.getElementById('proteinPercent').value);
    const carbs = parseInt(document.getElementById('carbsPercent').value);
    const fat = parseInt(document.getElementById('fatPercent').value);

    document.getElementById('proteinPercentValue').textContent = protein;
    document.getElementById('carbsPercentValue').textContent = carbs;
    document.getElementById('fatPercentValue').textContent = fat;

    document.getElementById('proteinBar').style.width = protein + '%';
    document.getElementById('proteinBar').setAttribute('aria-valuenow', protein);
    document.getElementById('carbsBar').style.width = carbs + '%';
    document.getElementById('carbsBar').setAttribute('aria-valuenow', carbs);
    document.getElementById('fatBar').style.width = fat + '%';
    document.getElementById('fatBar').setAttribute('aria-valuenow', fat);

    const total = protein + carbs + fat;
    document.getElementById('totalPercent').textContent = total;
    const alertDiv = document.getElementById('totalAlert');
    const msg = document.getElementById('totalMessage');
    if (total === 100) {
        alertDiv.className = 'alert alert-success mb-3';
        msg.innerHTML = '<i class="bi bi-check-circle-fill"></i> Perfect!';
    } else if (total < 100) {
        alertDiv.className = 'alert alert-warning mb-3';
        msg.innerHTML = '<i class="bi bi-exclamation-triangle-fill"></i> Need ' + (100 - total) + '% more';
    } else {
        alertDiv.className = 'alert alert-danger mb-3';
        msg.innerHTML = '<i class="bi bi-x-circle-fill"></i> Reduce by ' + (total - 100) + '%';
    }
}

function resetToRecommended() {
    const g = (currentGoalValue() || '').toLowerCase();
    if (!g) {
        alert('Please select a fitness goal first');
        return;
    }
    let p = 30, c = 40, f = 30;
    if (g.includes('lose')) {
        p = 35; c = 35; f = 30;
    } else if (g.includes('build')) {
        p = 30; c = 40; f = 30;
    } else if (g.includes('maintain')) {
        p = 30; c = 40; f = 30;
    }
    document.getElementById('proteinPercent').value = p;
    document.getElementById('carbsPercent').value = c;
    document.getElementById('fatPercent').value = f;
    updateMacroSplit();
}

// Wire up events when DOM is ready
document.addEventListener('DOMContentLoaded', function () {
    disableMacroSliders();
    updateRecommendedBanner();

    // Update banner and sliders on goal change
    document.querySelectorAll('input[name="fitnessGoal"]').forEach(r => {
        r.addEventListener('change', () => {
            enableMacroSliders();
            updateRecommendedBanner();
            resetToRecommended();
        });
    });

    // Validate on submit: total must be 100
    const form = document.getElementById('mealPlanForm');
    if (form) {
        form.addEventListener('submit', function (e) {
            const total = parseInt(document.getElementById('proteinPercent').value) +
                          parseInt(document.getElementById('carbsPercent').value) +
                          parseInt(document.getElementById('fatPercent').value);
            if (total !== 100) {
                e.preventDefault();
                alert('Macro percentages must total 100%. Current total: ' + total + '%');
                document.getElementById('proteinPercent').scrollIntoView({ behavior: 'smooth', block: 'center' });
                return false;
            }
        });
    }
});

