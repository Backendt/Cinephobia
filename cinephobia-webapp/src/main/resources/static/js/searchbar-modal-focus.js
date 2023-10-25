// "Due to how HTML5 defines its semantics, the autofocus HTML attribute has no effect in Bootstrap modals"
// https://getbootstrap.com/docs/5.3/components/modal/

const SEARCH_MODAL_QUERY = "#searchModal";
const SEARCHBAR_QUERY = "#searchbar";

function onSearchModalDisplayed() {
    let searchbar = document.querySelector(SEARCHBAR_QUERY);
    searchbar.focus();
}

function main() {
    window.addEventListener("DOMContentLoaded", () => {
        let searchModal = document.querySelector(SEARCH_MODAL_QUERY);
        searchModal.addEventListener('shown.bs.modal', onSearchModalDisplayed);
    });
}

main();

/* HTML Required:
<div id="searchModal" class="modal fade">
    ...
        <input id="searchbar" type="search">
    ...
</div>
*/