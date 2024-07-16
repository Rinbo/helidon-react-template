import { Role } from "../auth/auth.ts";
import React from "react";
import { fetcher } from "../utils/http.ts";

export type Principal = { name: string; email: string; roles: Role[] };

type StatusCallback = (status: boolean) => void;
type AuthenticationDetails = { email: string; token: string };

interface AuthContextType {
  principal: Principal | null;
  authenticate: (details: AuthenticationDetails, callback: StatusCallback) => void;
  signout: (callback: VoidFunction) => void;
}

const AuthContext = React.createContext<AuthContextType>(null!);

export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const [principal, setPrincipal] = React.useState<Principal | null>(null);

  const authenticate = async (authenticationDetails: AuthenticationDetails, callback: StatusCallback) => {
    const response = await fetcher({ path: "/auth/web/authenticate", body: authenticationDetails, method: "POST" });
    response.ok && setPrincipal(await response.json());
    callback(response.ok);
  };

  const signout = (callback: VoidFunction) => {
    console.log(callback());
  };

  let value = { principal, authenticate, signout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return React.useContext(AuthContext);
}
