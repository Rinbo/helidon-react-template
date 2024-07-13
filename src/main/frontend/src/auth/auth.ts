import { fetcher } from "../utils/http.ts";

export type Role = "ADMIN" | "USER" | "WEBMASTER";
export type Principal = { name: string; email: string; roles: Role[] };

interface AuthProvider {
  principal: Principal | null;
  signin(username: string): Promise<void>;
  signout(): Promise<void>;
  fetchPrincipal(): Promise<void>;
}

export const authProvider: AuthProvider = {
  principal: null,
  async signin(username: string) {
    console.log(username);
    await new Promise((r) => setTimeout(r, 500)); // fake delay
    // TODO set principal
  },
  async signout() {
    await new Promise((r) => setTimeout(r, 500)); // fake delay
    authProvider.principal = null;
  },
  async fetchPrincipal() {
    try {
      const response = await fetcher({ path: "/auth/web/principal" });
      const user = (await response.json()) satisfies Principal;
      console.log(user, "USER");
      authProvider.principal = user;
    } catch (error) {
      console.info("No active session");
    }
  },
};
