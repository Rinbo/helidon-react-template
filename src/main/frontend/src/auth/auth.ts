import { fetcher } from "../utils/http.ts";

export type Role = "ADMIN" | "USER" | "WEBMASTER";
export type Principal = { name: string; email: string; roles: Role[] };
export type PrincipalOption = Principal | null;

type AuthenticationDetails = { email: string; token: string };

interface AuthProvider {
  principal: PrincipalOption;
  authenticate(details: AuthenticationDetails): Promise<void>;
  logout(): Promise<void>;
  fetchPrincipal(): Promise<void>;
  isAuthenticated(): Promise<boolean>;
}

export const authProvider: AuthProvider = {
  principal: null,
  async authenticate(details: AuthenticationDetails) {
    const response = await fetcher({ path: `/auth/web/authenticate?${new URLSearchParams(details)}`, body: details, method: "POST" });
    authProvider.principal = (await response.json()) satisfies Principal;
  },
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
  async isAuthenticated(): Promise<boolean> {
    if (authProvider.principal) return true;
    await authProvider.fetchPrincipal();
    return !!authProvider.principal;
  },
};
