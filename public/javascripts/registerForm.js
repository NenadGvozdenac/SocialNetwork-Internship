let check = function() {
    let passwordValue = document.getElementById('password').value
    let passwordConfirmValue = document.getElementById('confirm-password').value
    let messageWhetherIsSameOrNot = document.getElementById('message')
    let registerButton = document.getElementById('register-button')

    if (passwordValue !== passwordConfirmValue) {
        messageWhetherIsSameOrNot.style.color = 'red';
        messageWhetherIsSameOrNot.innerHTML = 'Passwords do not match.';
        registerButton.disabled = true;
        registerButton.classList.remove("enabled")
    } else {
        messageWhetherIsSameOrNot.innerHTML = 'Passwords match!';
        messageWhetherIsSameOrNot.style.color = '#03fc73';
        registerButton.disabled = false
        registerButton.classList.add("enabled")
    }
}