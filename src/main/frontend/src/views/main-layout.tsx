import { Link, Outlet, useLoaderData } from "react-router-dom";
import { PrincipalOption } from "../auth/auth.ts";
import React, { createContext, useContext } from "react";
import AppLogo from "../components/navigation/app-logo.tsx";
import { IoAppsOutline } from "react-icons/io5";
import LogoutForm from "./logout/logout-form.tsx";

type AuthContextType = { principal: PrincipalOption };
const AuthContext = createContext<AuthContextType>(null!);

export function useAuth() {
  return useContext(AuthContext);
}

export default function MainLayout() {
  const principal = useLoaderData() as AuthContextType;

  return (
    <AuthContext.Provider value={principal}>
      <main className="flex grow flex-col p-2">
        <AppHeader />
        <SideBar />
        <div className="grow sm:container">
          <Outlet />
        </div>
        <div className="py-2 text-center font-mono text-sm">borjesson.dev</div>
      </main>
    </AuthContext.Provider>
  );
}

function AppHeader() {
  return (
    <header className="navbar flex flex-row bg-base-100">
      <AppLogo />
      <div className="grow" />
      <AppMenu />
    </header>
  );
}

function SideBar() {
  return (
    <aside className="left 0 fixed top-1/2 z-20 mx-1 hidden w-fit translate-y-[-50%] rounded px-2 py-4 sm:block">
      <div className="flex flex-col gap-4 rounded-lg bg-base-content bg-opacity-15 px-2 py-4">
        <div className="btn btn-primary h-12 w-12">1</div>
        <div className="btn btn-primary h-12 w-12">2</div>
        <div className="btn btn-primary h-12 w-12">3</div>
        <div className="btn btn-primary h-12 w-12">4</div>
      </div>
    </aside>
  );
}

function AppMenu() {
  const { principal } = useAuth();

  const loggedInProfile = (
    <React.Fragment>
      <li>
        <Link to="/profile">Profile</Link>
      </li>
      <li>
        <LogoutForm />
      </li>
    </React.Fragment>
  );

  const loggedOutProfile = (
    <li>
      <Link to="/login">Login</Link>
    </li>
  );

  return (
    <div className="dropdown dropdown-end">
      <div role="button" tabIndex={0} className="btn btn-ghost p-1">
        <IoAppsOutline className="text-3xl text-primary sm:text-4xl" />
      </div>
      <div tabIndex={0} className="menu dropdown-content z-[1] mt-2 w-screen max-w-sm rounded-box bg-base-300 p-2 shadow sm:max-w-xl">
        <div className="divider px-10 py-2 font-mono">borjessons.dev</div>

        <div className="row flex flex-wrap justify-center gap-4 p-4">
          <div className="btn btn-primary h-12 w-12">1</div>
          <div className="btn btn-primary h-12 w-12">2</div>
          <div className="btn btn-primary h-12 w-12">3</div>
          <div className="btn btn-primary h-12 w-12">4</div>
        </div>
        <ul className="menu p-4">{principal ? loggedInProfile : loggedOutProfile}</ul>
      </div>
    </div>
  );
}
