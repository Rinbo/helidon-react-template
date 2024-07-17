import { Outlet, useLoaderData } from "react-router-dom";
import { PrincipalOption } from "../auth/auth.ts";
import { createContext, useContext } from "react";
import AppHeader from "../components/navigation/header.tsx";
import SideBar from "../components/navigation/sidebar.tsx";

type AuthContextType = { principal: PrincipalOption };
const AuthContext = createContext<AuthContextType>(null!);

export default function MainLayout() {
  const principal = useLoaderData() as AuthContextType;

  return (
    <AuthContext.Provider value={principal}>
      <main className="flex grow flex-col p-2">
        <AppHeader />
        <SideBar />
        <div className="grow">
          <Outlet />
        </div>
        <div className="py-2 text-center font-mono text-sm">borjesson.dev</div>
      </main>
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
