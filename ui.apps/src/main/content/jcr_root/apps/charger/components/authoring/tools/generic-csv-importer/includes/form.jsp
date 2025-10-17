<form id="form">
    <div class="form-row">
            <h4 acs-coral-heading class="coral-Heading coral-Heading--4">Excel File</h4>
            <span>
                <input
                        accept="*/*"
                        type="file"
                        name="excel"
                        ngf-select
                        required
                        placeholder="Select the Categories CSV file"/>
            </span>
    </div>
    <div class="form-row">
        <h4 acs-coral-heading class="coral-Heading coral-Heading--4">Content Fragment Parent Path</h4>
        <span>
            <input type="text"
                   name="parentPath"
                   class="coral-Textfield"
                   required="true"
                   placeholder=""
            value="/content/dam/charger"/>
        </span>
    </div>
    <div class="form-row">
        <h4 acs-coral-heading class="coral-Heading coral-Heading--4">Model Type</h4>
        <span>
            <select required="true" name="modelType"  id="modelTypeDropdown" class="coral-Textfield">
              <option value="">Loading models...</option>
             </select>
        </span>
    </div>

</div>
    <div class="form-row">
        <div class="form-left-cell">&nbsp;</div>
        <button onclick="createContentFragment();return false;" class="coral-Button coral-Button--primary">Submit</button>
    </div>
</form>
<script>
document.addEventListener("DOMContentLoaded", function () {
    const dropdown = document.getElementById("modelTypeDropdown");
    fetch("/bin/getCFModels")
        .then(res => res.json())
        .then(models => {
            dropdown.innerHTML = ""; // clear old options
            if (models.length > 0) {
                models.forEach(model => {
                    const option = document.createElement("option");
                    option.value = model.name;
                    option.textContent = model.title || model.name;
                    dropdown.appendChild(option);
                });
            } else {
                const opt = document.createElement("option");
                opt.textContent = "No models found";
                dropdown.appendChild(opt);
            }
        })
        .catch(err => {
            console.error("Error loading models:", err);
            dropdown.innerHTML = "<option>Error loading models</option>";
        });
});
</script>



