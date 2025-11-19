// ============================================
// WIZARD NAVIGATION & VALIDATION
// ============================================

let currentStep = 1;
const totalSteps = 3;

// Initialize wizard on page load
document.addEventListener('DOMContentLoaded', function() {
    showStep(1);
});

// Show specific step
function showStep(stepNumber) {
    // Hide all steps
    document.querySelectorAll('.wizard-step').forEach(step => {
        step.classList.remove('active');
    });
    
    // Show current step
    const currentStepEl = document.querySelector(`.wizard-step[data-step="${stepNumber}"]`);
    if (currentStepEl) {
        currentStepEl.classList.add('active');
    }
    
    // Update progress indicator
    updateProgressIndicator(stepNumber);
    
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    currentStep = stepNumber;
}

// Update progress indicator
function updateProgressIndicator(stepNumber) {
    document.querySelectorAll('.step').forEach((step, index) => {
        const stepNum = index + 1;
        
        if (stepNum < stepNumber) {
            step.classList.add('completed');
            step.classList.remove('active');
        } else if (stepNum === stepNumber) {
            step.classList.add('active');
            step.classList.remove('completed');
        } else {
            step.classList.remove('active', 'completed');
        }
    });
}

// Navigate to next step
function nextStep(fromStep) {
    if (validateStep(fromStep)) {
        showStep(fromStep + 1);
    }
}

// Navigate to previous step
function prevStep(fromStep) {
    showStep(fromStep - 1);
}

// Validate current step before proceeding
function validateStep(stepNumber) {
    let isValid = true;
    const currentStepEl = document.querySelector(`.wizard-step[data-step="${stepNumber}"]`);
    
    // Clear previous errors
    currentStepEl.querySelectorAll('.form-group').forEach(group => {
        group.classList.remove('has-error');
    });
    
    if (stepNumber === 1) {
        // Validate Step 1: About You
        const age = document.getElementById('age').value;
        const sex = document.getElementById('sex').value;
        const heightFeet = document.getElementById('heightFeet').value;
        const heightInches = document.getElementById('heightInches').value;
        const weight = document.getElementById('weight').value;
        
        if (!age || age < 13 || age > 120) {
            showError('age', 'Please enter a valid age (13-120)');
            isValid = false;
        }
        
        if (!sex) {
            alert('Please select your biological sex');
            isValid = false;
        }
        
        if (!heightFeet || heightFeet < 3 || heightFeet > 8) {
            showError('heightFeet', 'Please enter valid height');
            isValid = false;
        }
        
        if (!heightInches || heightInches < 0 || heightInches > 11) {
            showError('heightInches', 'Please enter valid inches (0-11)');
            isValid = false;
        }
        
        if (!weight || weight < 50 || weight > 500) {
            showError('weight', 'Please enter a valid weight');
            isValid = false;
        }
    }
    
    if (stepNumber === 2) {
        // Validate Step 2: Activity & Goal
        const activityLevel = document.getElementById('activityLevel').value;
        const fitnessGoal = document.getElementById('fitnessGoal').value;
        
        if (!activityLevel) {
            alert('Please select your activity level');
            isValid = false;
        }
        
        if (!fitnessGoal) {
            alert('Please select your fitness goal');
            isValid = false;
        }
    }
    
    // Step 3 validation happens on form submit
    
    return isValid;
}

// Show error message
function showError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const formGroup = field.closest('.form-group');
    
    formGroup.classList.add('has-error');
    
    // Create error message if doesn't exist
    let errorMsg = formGroup.querySelector('.error-message');
    if (!errorMsg) {
        errorMsg = document.createElement('div');
        errorMsg.className = 'error-message';
        formGroup.appendChild(errorMsg);
    }
    errorMsg.textContent = message;
}

// ============================================
// SELECTION FUNCTIONS
// ============================================

// Select sex
function selectSex(value) {
    // Update hidden input
    document.getElementById('sex').value = value;
    
    // Update button states
    document.querySelectorAll('.button-group .option-button').forEach(btn => {
        btn.classList.remove('selected');
    });
    
    const selectedBtn = document.querySelector(`.option-button[data-value="${value}"]`);
    if (selectedBtn) {
        selectedBtn.classList.add('selected');
    }
}

// Select activity level
function selectActivity(value) {
    const map = {
        sedentary: "Sedentary",
        lightly_active: "Occasionally Active",
        moderately_active: "Moderately Active",
        very_active: "Very Active",
        extremely_active: "Extremely Active"
    };
    // Update hidden input with label expected by backend
    document.getElementById('activityLevel').value = map[value] || "";
    
    // Update card states
    document.querySelectorAll('.card-group .option-card').forEach(card => {
        if (card.getAttribute('onclick')?.includes('selectActivity')) {
            card.classList.remove('selected');
        }
    });
    
    const selectedCard = document.querySelector(`.option-card[data-value="${value}"]`);
    if (selectedCard && selectedCard.getAttribute('onclick')?.includes('selectActivity')) {
        selectedCard.classList.add('selected');
    }
}

// Select fitness goal
function selectGoal(value) {
    const map = {
        lose_weight: "Lose Weight",
        maintain: "Maintain Weight",
        build_muscle: "Build Muscle"
    };
    // Update hidden input with label expected by backend
    document.getElementById('fitnessGoal').value = map[value] || "";
    
    // Update card states
    document.querySelectorAll('.card-group .option-card').forEach(card => {
        if (card.getAttribute('onclick')?.includes('selectGoal')) {
            card.classList.remove('selected');
        }
    });
    
    const selectedCard = document.querySelector(`.option-card[data-value="${value}"]`);
    if (selectedCard && selectedCard.getAttribute('onclick')?.includes('selectGoal')) {
        selectedCard.classList.add('selected');
    }
}

// Select BMR formula
function selectFormula(value) {
    const map = {
        mifflin: "mifflin-st-jeor",
        harris: "harris-benedict",
        katch: "katch-mcardle"
    };
    // Update hidden input
    document.getElementById('bmrFormula').value = map[value] || "mifflin-st-jeor";
    
    // Update card states
    document.querySelectorAll('.card-group .option-card').forEach(card => {
        if (card.getAttribute('onclick')?.includes('selectFormula')) {
            card.classList.remove('selected');
        }
    });
    
    const selectedCard = document.querySelector(`.option-card[data-value="${value}"]`);
    if (selectedCard && selectedCard.getAttribute('onclick')?.includes('selectFormula')) {
        selectedCard.classList.add('selected');
    }
}

// ============================================
// FORM SUBMISSION
// ============================================

// Handle form submission
document.getElementById('createPlanForm')?.addEventListener('submit', function(e) {
    // Validate Step 3 before submitting
    if (!validateStep(3)) {
        e.preventDefault();
        return false;
    }
    
    // Show loading state
    const submitBtn = document.querySelector('.btn-submit');
    submitBtn.textContent = 'Generating Plan... ⚡';
    submitBtn.disabled = true;
    
    // Form will submit normally to existing endpoint
});

// ============================================
// KEYBOARD NAVIGATION
// ============================================

document.addEventListener('keydown', function(e) {
    // Enter key on inputs moves to next step
    if (e.key === 'Enter' && e.target.tagName === 'INPUT') {
        e.preventDefault();
        
        if (currentStep < totalSteps) {
            nextStep(currentStep);
        }
    }
});
