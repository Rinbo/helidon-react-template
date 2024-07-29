import React, { PropsWithChildren, ReactElement, useRef } from "react";

export const closeModal = () => {
  const modal = document.querySelector("dialog.modal") as HTMLDialogElement | null;
  if (modal) modal.close();
};

type Props = { actionElement: ReactElement } & PropsWithChildren;

export default function Modal({ actionElement, children }: Props) {
  const modalRef = useRef<HTMLDialogElement>(null);

  function onClick() {
    modalRef.current?.showModal();
  }

  const wrappedActionElement = <div onClick={onClick}>{actionElement}</div>;

  return (
    <React.Fragment>
      {wrappedActionElement}
      <dialog ref={modalRef} className="modal">
        <div className="modal-box">{children}</div>
      </dialog>
    </React.Fragment>
  );
}
