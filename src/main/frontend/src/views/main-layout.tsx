import { Link, NavLink, Outlet, useRouteLoaderData } from "react-router-dom";
import { PrincipalOption } from "../auth/auth.ts";
import React from "react";
import AppLogo from "../components/navigation/app-logo.tsx";
import { IoAppsOutline } from "react-icons/io5";
import LogoutForm from "./logout/logout-form.tsx";
import { MenuItem, menuItems } from "../routes.tsx";
import { iconLib } from "../utils/icon-lib.ts";

export function useAuth() {
  return useRouteLoaderData("root") as { principal: PrincipalOption };
}

export default function MainLayout() {
  return (
    <main className="flex grow flex-col p-2">
      <AppHeader />
      <SideBar />
      <div className="grow sm:container">
        <Outlet />
      </div>
      <Footer />
    </main>
  );
}

export function AppHeader() {
  return (
    <header className="navbar flex flex-row bg-base-100">
      <AppLogo />
      <div className="grow" />
      <AppMenu />
    </header>
  );
}

export function Footer() {
  return <div className="pb-2 pt-6 text-center font-mono text-sm">borjesson.dev</div>;
}

function CustomNavLink({ item }: { item: MenuItem }) {
  const Icon = iconLib[item.icon];

  return (
    <div className="tooltip tooltip-right" data-tip={item.label}>
      <NavLink className={({ isActive }) => `btn btn-primary h-12 w-12 p-1 ${isActive ? "active" : ""}`} to={item.path}>
        {Icon && <Icon className="text-xl" />}
      </NavLink>
    </div>
  );
}

function useNavLinks(principal: PrincipalOption) {
  return menuItems.filter((item) => !item.requireAuth || principal);
}

function SideBar() {
  const { principal } = useAuth();
  const navLinks = useNavLinks(principal);

  return (
    <aside className="left 0 fixed top-1/2 z-20 mx-1 hidden w-fit translate-y-[-50%] rounded px-2 py-4 sm:block">
      <div className="flex flex-col gap-4 rounded-lg bg-base-content bg-opacity-15 px-2 py-4">
        {navLinks.map((item) => (
          <CustomNavLink key={item.label} item={item} />
        ))}
      </div>
    </aside>
  );
}

function AppMenu() {
  const { principal } = useAuth();
  const navLinks = useNavLinks(principal);

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
      <div className="menu dropdown-content z-10 mt-2 w-80 overflow-hidden rounded-box bg-base-300 p-2 shadow sm:w-screen sm:max-w-xl">
        <div className="divider px-10 py-2 font-mono">borjessons.dev</div>

        <div className="row flex flex-wrap justify-center gap-4 p-4">
          {navLinks.map((item) => (
            <CustomNavLink key={item.label} item={item} />
          ))}
        </div>
        <ul className="menu p-4">{principal ? loggedInProfile : loggedOutProfile}</ul>
      </div>
    </div>
  );
}
