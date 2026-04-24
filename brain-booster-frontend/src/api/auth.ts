const BASE_API_URL = process.env.NEXT_PUBLIC_API_URL;

export async function registerUser(data: {
  nickname: string;
  email: string;
  password: string;
}) {
  const response = await fetch(`${BASE_API_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  if (response.status !== 201) {
    let errMsg: string;
    const text = await response.text();
    try {
      const { message } = JSON.parse(text);
      errMsg = message || text;
    } catch {
      errMsg = text;
    }
    throw new Error(errMsg);
  }

  return response.text();
}

export async function authenticateUser(data: {
  email: string;
  password: string;
}) {
  const response = await fetch(`${BASE_API_URL}/auth/authenticate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    let errMsg: string;
    const text = await response.text();
    try {
      const { message } = JSON.parse(text);
      errMsg = message || "Login failed";
    } catch {
      errMsg = text || "Login failed";
    }
    throw new Error(errMsg);
  }

  const result = await response.json();
  return result.token;
}
