import { fetcher } from "../utils/http.ts";

export type Role = "ADMIN" | "USER" | "WEBMASTER";
export type Principal = { name: string; email: string; roles: Role[] };

interface AuthProvider {
  principal: Principal | null;
  logout(): Promise<void>;
  fetchPrincipal(): Promise<void>;
}

export const authProvider: AuthProvider = {
  principal: null,
  async logout() {
    await fetcher({ path: "/auth/web/logout", method: "POST" });
    authProvider.principal = null;
  },
  async fetchPrincipal() {
    try {
      const response = await fetcher({ path: "/auth/web/principal" });
      authProvider.principal = (await response.json()) satisfies Principal;
    } catch (error) {
      console.info("No active session");
    }
  },
};
