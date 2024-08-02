import Breadcrumb from "./breadcrumb.tsx";
import { PropsWithChildren } from "react";

export default function ContextMenu(props: PropsWithChildren) {
  return (
    <div className="flex w-full flex-row items-center justify-center rounded-lg border border-neutral px-3 py-1">
      <Breadcrumb />
      <div className="grow" />
      {props.children && props.children}
    </div>
  );
}
