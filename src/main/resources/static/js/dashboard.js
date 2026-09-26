const state = {
  transactions: [],
  categories: [],
  transactionType: "EXPENSE",
};

const money = (value) =>
  `₹ ${Number(value || 0).toLocaleString("en-IN", {
    maximumFractionDigits: 2,
  })}`;

const today = () => new Date().toISOString().slice(0, 10);

async function api(url, options = {}) {
  const response = await fetch(url, {
    credentials: "same-origin",
    ...options,
  });

  if (response.status === 401) {
    window.location.href = "login.html";
    throw new Error("Session expired");
  }

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.message || "Request failed");
  }

  return data;
}

function showSection(id) {
  document
    .querySelectorAll(".page-section")
    .forEach((s) => s.classList.toggle("active", s.id === id));

  document
    .querySelectorAll(".nav-btn[data-section]")
    .forEach((b) => b.classList.toggle("active", b.dataset.section === id));

  window.scrollTo({
    top: 0,
    behavior: "smooth",
  });
}

/* =========================
   SIDEBAR NAVIGATION
========================= */

document
  .querySelectorAll(".nav-btn[data-section]")
  .forEach((btn) =>
    btn.addEventListener("click", () => showSection(btn.dataset.section)),
  );

document
  .querySelectorAll("[data-section-go]")
  .forEach((btn) =>
    btn.addEventListener("click", () => showSection(btn.dataset.sectionGo)),
  );

/* =========================
   LOGOUT
========================= */

document.getElementById("logoutBtn").addEventListener("click", async () => {
  try {
    await api("api/auth/logout", {
      method: "POST",
    });
  } finally {
    window.location.href = "login.html";
  }
});

/* =========================
   LOAD CATEGORIES
========================= */

async function loadCategories() {
  state.categories = await api("api/categories");

  /* Category dropdown in Add Transaction */
  const category = document.getElementById("category");

  category.innerHTML = state.categories
    .map((c) => `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`)
    .join("");

  /* Category filter in Transactions */
  const filter = document.getElementById("filterCategory");

  filter.innerHTML =
    '<option value="">All Categories</option>' +
    state.categories.map((c) => `<option>${escapeHtml(c)}</option>`).join("");

  /* Category cards */
  const categoryCards = document.getElementById("categoryCards");

  categoryCards.innerHTML = state.categories
    .map(
      (c, i) =>
        `
        <article
          class="category-card"
          data-category="${escapeHtml(c)}"
        >
          <div class="cat-card-icon cat-${i % 6}">
            ${categoryIcon(c)}
          </div>

          <h3>${escapeHtml(c)}</h3>

          <p>
            Click to view ${escapeHtml(c)} transactions.
          </p>
        </article>
        `,
    )
    .join("");

  /*
   * CATEGORY CARD CLICK
   *
   * Example:
   * Food → Transactions → Food filter
   */
  document.querySelectorAll(".category-card").forEach((card) => {
    card.addEventListener("click", () => {
      const selectedCategory = card.dataset.category;

      /* Select the category in the transaction filter */
      document.getElementById("filterCategory").value = selectedCategory;

      /* Refresh transaction list */
      renderTransactions();

      /* Open Transactions section */
      showSection("transactions");
    });
  });
}

/* =========================
   LOAD ALL DATA
========================= */

async function loadAll() {
  const [dashboard, transactions, profile] = await Promise.all([
    api("api/dashboard"),
    api("api/transactions"),
    api("api/profile"),
  ]);

  state.transactions = transactions;

  renderDashboard(dashboard);
  renderTransactions();
  renderProfile(profile);
}

/* =========================
   DASHBOARD
========================= */

function renderDashboard(data) {
  document.getElementById("helloName").textContent = data.name;

  document.getElementById("topUserName").textContent = data.name;

  document.querySelector(".avatar").textContent = (data.name || "U")
    .slice(0, 1)
    .toUpperCase();

  document.getElementById("todayText").textContent = new Date(
    `${data.currentDate}T00:00:00`,
  ).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });

  document.getElementById("totalIncome").textContent = money(data.totalIncome);

  document.getElementById("totalExpense").textContent = money(
    data.totalExpense,
  );

  document.getElementById("remainingBalance").textContent = money(
    data.remainingBalance,
  );
  document.getElementById("monthlyBudget").textContent = money(
    data.monthlyBudget,
  );

  document.getElementById("totalTransactions").textContent =
    data.totalTransactions;

  renderChart(data.chart);
  renderRecent();
  renderCategorySummary(data.categoryTotals);
}

/* =========================
   CHART
========================= */

function renderChart(points) {
  const max = Math.max(
    20000,
    ...points.flatMap((p) => [Number(p.income || 0), Number(p.expense || 0)]),
  );

  document.getElementById("barChart").innerHTML = points
    .map((p) => {
      const income = Math.max(4, (Number(p.income || 0) / max) * 100);

      const expense = Math.max(4, (Number(p.expense || 0) / max) * 100);

      return `
        <div class="bar-group">

          <div class="bars">

            <span
              class="bar income-bar"
              style="height:${income}%"
              title="Income ${money(p.income)}"
            ></span>

            <span
              class="bar expense-bar"
              style="height:${expense}%"
              title="Expense ${money(p.expense)}"
            ></span>

          </div>

          <small>
            ${p.label.replace(" ", "<br>")}
          </small>

        </div>
      `;
    })
    .join("");
}

/* =========================
   RECENT TRANSACTIONS
========================= */

function renderRecent() {
  const items = state.transactions.slice(0, 5);

  document.getElementById("recentTransactions").innerHTML = items.length
    ? items
        .map(
          (t) =>
            `
              <div class="recent-item">

                <span
                  class="recent-icon ${
                    t.type === "INCOME" ? "recent-income" : "recent-expense"
                  }"
                >
                  ${categoryIcon(t.category)}
                </span>

                <div class="recent-info">

                  <strong>
                    ${escapeHtml(t.description || t.category)}
                  </strong>

                  <small>
                    ${escapeHtml(t.category)}
                  </small>

                </div>

                <div
                  class="recent-amount ${
                    t.type === "INCOME" ? "positive" : "negative"
                  }"
                >
                  ${t.type === "INCOME" ? "+" : "-"}
                  ${money(t.amount)}

                  <small>
                    ${formatDate(t.transactionDate)}
                  </small>
                </div>

              </div>
              `,
        )
        .join("")
    : '<div class="empty-state">No transactions yet.</div>';
}

/* =========================
   CATEGORY SUMMARY
========================= */

function renderCategorySummary(categoryTotals) {
  const entries = Object.entries(categoryTotals || {});

  const total = entries.reduce((s, [, v]) => s + Number(v), 0) || 1;

  const top = entries.sort((a, b) => Number(b[1]) - Number(a[1])).slice(0, 6);

  document.getElementById("categorySummary").innerHTML = top.length
    ? top
        .map(
          ([name, amount], i) =>
            `
              <div class="category-row">

                <div class="cat-name">

                  <span
                    class="mini-dot dot-${i % 6}"
                  ></span>

                  <span>
                    ${escapeHtml(name)}
                  </span>

                  <b>
                    ${Math.round((Number(amount) / total) * 100)}%
                  </b>

                </div>

                <div class="progress">

                  <span
                    class="progress-fill fill-${i % 6}"
                    style="width:${(Number(amount) / total) * 100}%"
                  ></span>

                </div>

              </div>
              `,
        )
        .join("")
    : '<div class="empty-state">No expense categories this month.</div>';
}

/* =========================
   TRANSACTIONS
========================= */

function renderTransactions() {
  const search = document
    .getElementById("searchInput")
    .value.trim()
    .toLowerCase();

  const type = document.getElementById("filterType").value;

  const category = document.getElementById("filterCategory").value;

  const filtered = state.transactions.filter((t) => {
    const text = `${t.category} ${t.description || ""}`.toLowerCase();

    return (
      (!search || text.includes(search)) &&
      (!type || t.type === type) &&
      (!category || t.category === category)
    );
  });

  document.getElementById("transactionsBody").innerHTML = filtered.length
    ? filtered
        .map(
          (t, i) =>
            `
              <tr>

                <td>
                  ${i + 1}
                </td>

                <td>
                  ${formatDate(t.transactionDate)}
                </td>

                <td>

                  <span
                    class="type-pill ${t.type.toLowerCase()}"
                  >
                    ${t.type}
                  </span>

                </td>

                <td>

                  <span class="tag">
                    ${escapeHtml(t.category)}
                  </span>

                </td>

                <td>
                  ${escapeHtml(t.description || "—")}
                </td>

                <td
                  class="money-cell ${
                    t.type === "INCOME" ? "positive" : "negative"
                  }"
                >
                  ${t.type === "INCOME" ? "+" : "-"}
                  ${money(t.amount)}
                </td>

                <td>

                  <button
                    class="action edit"
                    onclick="editTransaction(${t.id})"
                  >
                    Edit
                  </button>

                  <button
                    class="action delete"
                    onclick="deleteTransaction(${t.id})"
                  >
                    Delete
                  </button>

                </td>

              </tr>
              `,
        )
        .join("")
    : `
        <tr>
          <td
            colspan="7"
            class="empty-state"
          >
            No matching transactions.
          </td>
        </tr>
      `;
}

/* =========================
   SEARCH / FILTER
========================= */

document
  .getElementById("searchInput")
  .addEventListener("input", renderTransactions);

document
  .getElementById("filterType")
  .addEventListener("change", renderTransactions);

document
  .getElementById("filterCategory")
  .addEventListener("change", renderTransactions);

/* =========================
   INCOME / EXPENSE BUTTONS
========================= */

document.querySelectorAll(".type-btn").forEach((btn) =>
  btn.addEventListener("click", () => {
    state.transactionType = btn.dataset.type;

    document
      .querySelectorAll(".type-btn")
      .forEach((b) => b.classList.toggle("active", b === btn));

    document.querySelector("#transactionForm .primary-btn").textContent =
      state.transactionType === "INCOME" ? "Save Income" : "Save Expense";
  }),
);

/* =========================
   DEFAULT DATE
========================= */

document.getElementById("transactionDate").value = today();

/* =========================
   ADD / UPDATE TRANSACTION
========================= */

document
  .getElementById("transactionForm")
  .addEventListener("submit", async (e) => {
    e.preventDefault();

    const id = document.getElementById("transactionId").value;

    const payload = {
      amount: Number(document.getElementById("amount").value),

      type: state.transactionType,

      category: document.getElementById("category").value,

      description: document.getElementById("description").value,

      transactionDate: document.getElementById("transactionDate").value,
    };

    try {
      await api(id ? `api/transactions/${id}` : "api/transactions", {
        method: id ? "PUT" : "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify(payload),
      });

      setMessage(
        "transactionMessage",
        id
          ? "Transaction updated successfully."
          : "Transaction added successfully.",
        true,
      );

      resetForm();

      await loadAll();

      showSection("dashboard");
    } catch (err) {
      setMessage("transactionMessage", err.message, false);
    }
  });

/* =========================
   RESET FORM
========================= */

document.getElementById("resetForm").addEventListener("click", resetForm);

function resetForm() {
  document.getElementById("transactionId").value = "";

  document.getElementById("transactionForm").reset();

  document.getElementById("transactionDate").value = today();

  state.transactionType = "EXPENSE";

  document
    .querySelectorAll(".type-btn")
    .forEach((b) => b.classList.toggle("active", b.dataset.type === "EXPENSE"));

  document.querySelector("#transactionForm .primary-btn").textContent =
    "Save Expense";
}

/* =========================
   EDIT TRANSACTION
========================= */

window.editTransaction = function (id) {
  const t = state.transactions.find((x) => x.id === id);

  if (!t) return;

  document.getElementById("transactionId").value = t.id;

  document.getElementById("amount").value = t.amount;

  document.getElementById("category").value = t.category;

  document.getElementById("description").value = t.description || "";

  document.getElementById("transactionDate").value = t.transactionDate;

  state.transactionType = t.type;

  document
    .querySelectorAll(".type-btn")
    .forEach((b) => b.classList.toggle("active", b.dataset.type === t.type));

  document.querySelector("#transactionForm .primary-btn").textContent =
    t.type === "INCOME" ? "Update Income" : "Update Expense";

  showSection("add");
};

/* =========================
   DELETE TRANSACTION
========================= */

window.deleteTransaction = async function (id) {
  if (!confirm("Delete this transaction?")) {
    return;
  }

  try {
    await api(`api/transactions/${id}`, {
      method: "DELETE",
    });

    await loadAll();
  } catch (err) {
    alert(err.message);
  }
};

/* =========================
   PROFILE
========================= */

document.getElementById("profileForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  try {
    const data = await api("api/profile", {
      method: "PUT",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        name: document.getElementById("profileName").value,

        monthlyBudget: Number(document.getElementById("profileBudget").value),
      }),
    });

    renderProfile(data);

    setMessage("profileMessage", "Profile updated successfully.", true);

    await loadAll();
  } catch (err) {
    setMessage("profileMessage", err.message, false);
  }
});

function renderProfile(data) {
  document.getElementById("profileName").value = data.name;

  document.getElementById("profileEmail").value = data.email;

  document.getElementById("profileBudget").value = data.monthlyBudget;

  document.getElementById("topUserName").textContent = data.name;
}

/* =========================
   DATE FORMAT
========================= */

function formatDate(value) {
  return new Date(`${value}T00:00:00`).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

/* =========================
   MESSAGE
========================= */

function setMessage(id, text, success) {
  const el = document.getElementById(id);

  el.className = `message ${success ? "success" : "error"}`;

  el.textContent = text;
}

/* =========================
   HTML ESCAPE
========================= */

function escapeHtml(value) {
  return String(value).replace(
    /[&<>'"]/g,
    (c) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "'": "&#39;",
        '"': "&quot;",
      })[c],
  );
}

/* =========================
   CATEGORY ICONS
========================= */

function categoryIcon(category) {
  return (
    {
      Food: "🍴",
      Transport: "🚗",
      Shopping: "🛍",
      Bills: "▤",
      Education: "▣",
      Health: "♥",
      Entertainment: "♪",
      Other: "•",
    }[category] || "•"
  );
}

/* =========================
   INITIALIZE DASHBOARD
========================= */

(async function init() {
  try {
    await api("api/auth/me");

    await loadCategories();

    await loadAll();
  } catch (err) {
    console.error(err);
  }
})();
