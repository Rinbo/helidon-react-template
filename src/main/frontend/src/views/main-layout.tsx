import { Outlet, useLoaderData } from "react-router-dom";
import { PrincipalOption } from "../auth/auth.ts";
import { createContext, useContext } from "react";
import AppHeader from "../components/navigation/header.tsx";

type AuthContextType = { principal: PrincipalOption };
const AuthContext = createContext<AuthContextType>(null!);

export default function MainLayout() {
  const principal = useLoaderData() as AuthContextType;

  return (
    <AuthContext.Provider value={principal}>
      <main className="flex grow flex-col p-2 sm:p-4">
        <AppHeader />
        <div className="grow">
          <Outlet />
        </div>
        <div>FOOTER</div>
      </main>
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
