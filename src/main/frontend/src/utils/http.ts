import { PrincipalOption } from "../auth/auth.ts";

interface Fetcher {
  path: string;
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: object;
  headers?: Record<string, string>;
}

export function fetcher({ path, method, body, headers }: Fetcher): Promise<Response> {
  return fetch(path, {
    method,
    body: body && JSON.stringify(body),
    headers: {
      withCredentials: "true",
      "Content-Type": "application/json",
      ...headers,
    },
  });
}

export async function extractErrorMessage(response: Response): Promise<string> {
  if (response.headers.get("Content-Type")?.includes("application/json")) {
    try {
      const json = await response.json();
      return json.details || json.message || "Unknown error";
    } catch {
      // JSON parsing failed, fall through to text
    }
  }

  try {
    return (await response.text()) || "Unknown error";
  } catch {
    return "Unknown error";
  }
}

export function isAdmin(principal: PrincipalOption) {
  if (!principal) return false;
  return principal.roles.includes("ADMIN");
}
