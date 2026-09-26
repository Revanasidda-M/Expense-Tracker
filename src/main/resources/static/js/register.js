const form = document.getElementById("registerForm");
const message = document.getElementById("message");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  message.className = "message";
  message.textContent = "Creating your account...";

  try {
    const response = await fetch("api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "same-origin",
      body: JSON.stringify({
        name: document.getElementById("name").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        monthlyBudget: Number(
          document.getElementById("monthlyBudget").value || 0,
        ),
      }),
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || "Registration failed");
    window.location.href = "dashboard.html";
  } catch (error) {
    message.className = "message error";
    message.textContent = error.message;
  }
});
