document.addEventListener("DOMContentLoaded", () => {

    const totalInput = document.getElementById("totalPriceInput");
    const coverageInput = document.getElementById("coverageInput");
    const costPriceInput = document.getElementById("costPriceInput");

    const costPrice = parseFloat(totalInput.dataset.costprice);
    const originalTotal = parseFloat(totalInput.dataset.original);

    function update()
    {
        const total = parseFloat(totalInput.value) || 0;

        const priceWithoutVat = (total / 1.25);
        const priceWithVat = total;
        const coverage = ((priceWithoutVat - costPrice) / priceWithoutVat) * 100;
        const diff = total - originalTotal;

        document.getElementById("priceWithOutVatDisplay").textContent = priceWithoutVat.toFixed(2);
        document.getElementById("costPriceDisplay").textContent = costPrice.toFixed(2);
        document.getElementById("coverageDisplay").textContent = coverage.toFixed(1);
        document.getElementById("priceWithoutVatDisplay2").textContent = priceWithoutVat.toFixed(2);
        document.getElementById("priceWithVatDisplay").textContent = priceWithVat.toFixed(2);
        document.getElementById("differenceDisplay").textContent = diff.toFixed(2);

        coverageInput.value = coverage.toFixed(1);
        costPriceInput.value = costPrice.toFixed(2);
    }

    totalInput.addEventListener("input", update);
    update();
});