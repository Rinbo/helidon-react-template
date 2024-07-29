import React, { ReactElement } from "react";

type Props = {
  icon: ReactElement;
  action?: () => void;
  tooltip?: string;
};
export default function ButtonIcon({ icon, tooltip }: Props) {
  return (
    <button className="btn btn-ghost btn-sm tooltip tooltip-bottom flex px-1" data-tip={tooltip}>
      {React.cloneElement(icon, { className: icon.props.className ?? "text-2xl text-accent sm:text-3xl" })}
    </button>
  );
}
