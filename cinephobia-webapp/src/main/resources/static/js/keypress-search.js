const OPEN_SEARCHBAR_BUTTON = "#openSearchButton";
const SEARCHBAR_MODAL = "#searchModal";
const SEARCHBAR = "#searchbar";

function openSearchbar(event) {
    let isAlreadyOpen = document.querySelector(SEARCHBAR_MODAL).classList.contains("show");
    if(isAlreadyOpen) {
        return;
    }
    let searchButton = document.querySelector(OPEN_SEARCHBAR_BUTTON);
    searchButton.click();
    let searchModal = document.querySelector(SEARCHBAR);

    searchModal.focus();
    searchModal.value += event.key;
}

function onDOMLoaded() {
    let letterA = 65;
    let letterZ = 90;
    window.addEventListener("keydown", (event) => {
        let isCharacter = letterZ >= event.keyCode && event.keyCode >= letterA;
        if(!event.isComposing && isCharacter) {
            openSearchbar(event);
        }
    });
}

function main() {
    window.addEventListener("DOMContentLoaded", onDOMLoaded);
}

main();