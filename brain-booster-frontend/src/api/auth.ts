const BASE_API_URL = process.env.NEXT_PUBLIC_API_URL;

export async function registerUser(data: {
  nickname: string;
  email: string;
  password: string;
}) {
  const response = await fetch(`${BASE_API_URL}/api/v1/auth/register`, {
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
