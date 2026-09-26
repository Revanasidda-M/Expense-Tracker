const form = document.getElementById("loginForm");
const message = document.getElementById("message");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  message.className = "message";
  message.textContent = "Signing in...";

  try {
    const response = await fetch("api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "same-origin",
      body: JSON.stringify({
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
      }),
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || "Login failed");
    window.location.href = "dashboard.html";
  } catch (error) {
    message.className = "message error";
    message.textContent = error.message;
  }
});
