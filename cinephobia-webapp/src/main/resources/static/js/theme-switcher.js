// This script is inspired of Bootstrap's color mode toggler (https://getbootstrap.com/)

const THEME_BUTTON_QUERY = "#theme-toggler";
const THEME_ICON_QUERY = "#theme-toggler-icon";

function getStoredTheme() {
    return localStorage.getItem("theme");
}

function setStoredTheme(theme) {
    localStorage.setItem("theme", theme);
}

function getPreferredTheme() {
    let storedTheme = getStoredTheme();
    if(storedTheme) {
        return storedTheme;
    }

    let browserTheme = window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    return browserTheme;
}

function setTheme(newTheme) {
    document.documentElement.setAttribute("data-bs-theme", newTheme);
    setStoredTheme(newTheme);
}

function refreshThemeButtonIcon() {
    let currentTheme = getPreferredTheme();
    let themeClassAttribute = currentTheme === "dark" ? "class-dark" : "class-light";

    let themeIcon = document.querySelector(THEME_ICON_QUERY);
    let themeClass = themeIcon.getAttribute(themeClassAttribute);

    themeIcon.className = themeClass;
}

function toggleTheme() {
    let currentTheme = getPreferredTheme();
    let newTheme = currentTheme === "dark" ? "light" : "dark";
    setTheme(newTheme);

    refreshThemeButtonIcon();
}

function main() {
    setTheme(getPreferredTheme());

    window.addEventListener("DOMContentLoaded", () => {
        refreshThemeButtonIcon();

        let themeButton = document.querySelector(THEME_BUTTON_QUERY);
        themeButton.addEventListener("click", toggleTheme);
    });
}

main();

/* HTML Required:
<button id="theme-toggler" class="nav-link btn">
    <span id="theme-toggler-icon" class-light="bi bi-sun" class-dark="bi bi-moon"></span>
</button>
*/