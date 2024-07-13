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
