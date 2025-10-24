<form id="cfForm" class="coral-Form coral-Form--vertical">
  <div class="form-row">
    <h4 class="coral-Heading coral-Heading--4">Excel File</h4>
    <input
      type="file"
      name="excel"
      id="excelFile"
      accept=".xls,.xlsx"
      required
      class="coral-Textfield"
      placeholder="Select Excel file"
    />
  </div>

  <div class="form-row">
    <h4 class="coral-Heading coral-Heading--4">Content Fragment Parent Path</h4>
    <input
      type="text"
      name="parentPath"
      class="coral-Textfield"
      required
      value="/content/dam/charger"
    />
  </div>

  <div class="form-row">
    <h4 class="coral-Heading coral-Heading--4">Model Type</h4>
    <select
      id="modelTypeDropdown"
      name="modelType"
      class="coral-Textfield"
      required
    >
      <option value="">Loading models...</option>
    </select>
  </div>

  <div class="form-row">
    <h4 class="coral-Heading coral-Heading--4">Content Fragment Name</h4>
    <select
      id="selectfield"
      name="selectfield"
      class="coral-Textfield"
      required
      disabled
    >
      <option value="">Select Excel file to load names...</option>
    </select>
  </div>

  <div class="form-row">
    <button type="submit" class="coral-Button coral-Button--primary">
      Submit
    </button>
  </div>

  <div class="form-row">
    <button type="button" id="updateBtn" class="coral-Button coral-Button--secondary">
      Update
    </button>
  </div>
</form>

<style>
  .form-row {
    margin-bottom: 1rem;
  }
  .coral-Heading--4 {
    margin-bottom: 0.5rem;
  }
</style>

<script>
document.addEventListener("DOMContentLoaded", () => {
  initForm();
});

async function initForm() {
  // Load CF Models initially
  await populateDropdown("/bin/getCFModels", "modelTypeDropdown", "No models found");

  // Event listeners
  document.getElementById("cfForm").addEventListener("submit", handleSubmit);
  document.getElementById("excelFile").addEventListener("change", handleExcelUpload);
}

/**
 * Populate dropdowns dynamically from servlet response
 */
async function populateDropdown(url, dropdownId, emptyMessage) {
  const dropdown = document.getElementById(dropdownId);
  dropdown.disabled = true;
  dropdown.innerHTML = `<option>Loading...</option>`;

  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP error ${response.status}`);
    const items = await response.json();

    dropdown.innerHTML = "";
    if (Array.isArray(items) && items.length > 0) {
      items.forEach(item => {
        const option = document.createElement("option");
        option.value = item.name;
        option.textContent = item.title || item.name;
        dropdown.appendChild(option);
      });
    } else {
      dropdown.innerHTML = `<option>${emptyMessage}</option>`;
    }
  } catch (error) {
    console.error(`Error loading ${dropdownId}:`, error);
    dropdown.innerHTML = `<option>Error loading data</option>`;
  } finally {
    dropdown.disabled = false;
  }
}

/**
 * Triggered when an Excel file is uploaded
 * Sends POST request to /bin/getCFNames and populates name dropdown
 */
async function handleExcelUpload() {
  const file = document.getElementById("excelFile").files[0];
  const nameDropdown = document.getElementById("selectfield");

  if (!file) return;

  nameDropdown.disabled = true;
  nameDropdown.innerHTML = `<option>Loading field names...</option>`;

  const formData = new FormData();
  formData.append("excel", file);

  try {
    // ✅ Get CSRF token
    const csrfToken = await getCsrfToken();

    // ✅ Externalize URL to make it safe for AEM
    const url = Granite.HTTP.externalize("/bin/getCFNames");

    // ✅ POST with CSRF token in header
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "CSRF-Token": csrfToken
      },
      body: formData,
      credentials: "same-origin"
    });

    if (!response.ok) throw new Error(`Failed to load column names (${response.status})`);

    const names = await response.json();
    nameDropdown.innerHTML = "";

    if (Array.isArray(names) && names.length > 0) {
      names.forEach(name => {
        const option = document.createElement("option");
        option.value = name;
        option.textContent = name;
        nameDropdown.appendChild(option);
      });
    } else {
      nameDropdown.innerHTML = `<option>No column names found</option>`;
    }
  } catch (err) {
    console.error("Error fetching CF names:", err);
    nameDropdown.innerHTML = `<option>Error loading names</option>`;
  } finally {
    nameDropdown.disabled = false;
  }
}

/**
 * Handles form submission — creates content fragment
 */
async function handleSubmit(event) {
  event.preventDefault();
  const form = document.getElementById("cfForm");

  if (!form.checkValidity()) {
    alert("Please fill all required fields.");
    return;
  }

  try {
    const formData = new FormData(form);

    // ✅ Get CSRF token
    const csrfToken = await getCsrfToken();

    // ✅ Externalize URL (important when running in AEM)
    const url = Granite.HTTP.externalize("/bin/content/createContentFragment");

    // ✅ Send POST with CSRF token and same-origin credentials
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "CSRF-Token": csrfToken
      },
      body: formData,
      credentials: "same-origin"
    });

    if (!response.ok) {
      const text = await response.text();
      console.error("Response text:", text);
      throw new Error(`HTTP ${response.status} - ${response.statusText}`);
    }

    // ✅ Parse JSON safely
    const result = await response.json();
    alert(result.status || "Content Fragment created successfully!");
  } catch (err) {
    console.error("Error submitting form:", err);
    alert("Failed to create Content Fragment. Check console for details.");
  }
}

// 🔒 Fetch CSRF token from AEM
async function getCsrfToken() {
  const res = await fetch(Granite.HTTP.externalize("/libs/granite/csrf/token.json"), {
    credentials: "same-origin"
  });
  const data = await res.json();
  return data.token;
}


async function handleUpdate() {
  const form = document.getElementById("cfForm");

  if (!form.checkValidity()) {
    alert("Please fill all required fields before updating.");
    return;
  }



  const formData = new FormData(form);
  formData.append("mode", "update"); // you can remove mode entirely if servlet doesn’t need it

  try {
    const csrfToken = await getCsrfToken();
    const url = Granite.HTTP.externalize("/bin/updateCFs");

    const response = await fetch(url, {
      method: "POST",
      headers: { "CSRF-Token": csrfToken },
      body: formData,
      credentials: "same-origin"
    });

    if (!response.ok) throw new Error(`HTTP ${response.status}`);

    const result = await response.json();
     const updatedFragments = Array.isArray(result.updatedFragments) ? result.updatedFragments : [];

     let cf = "Updated CFs: ";
     for (let i = 0; i < updatedFragments.length; i++) {
       cf += updatedFragments[i] + " ";
     }

     const userConfirmed = confirm(
       updatedFragments.length > 0
         ? "Are you sure you want to update existing Content Fragments based on this Excel?\n\n" + cf
         : "No Content Fragments to update."
     );

     if (!userConfirmed) {
       alert("⚠️ Update cancelled by user.");
       return;
     }




    console.log("Update result:", result);

  } catch (err) {
    console.error("Error updating CFs:", err);
    alert("Failed to update CFs. Check console for details.");
  }
}

document.getElementById("updateBtn").addEventListener("click", handleUpdate);





</script>
