import React, { PropsWithChildren, ReactElement, useRef } from "react";

export const closeModal = (id: string) => {
  const modal = document.querySelector("#" + id) as HTMLDialogElement | null;
  if (modal) modal.close();
};

type Props = { actionElement: ReactElement; id: string } & PropsWithChildren;

export default function Modal({ actionElement, children, id }: Props) {
  const modalRef = useRef<HTMLDialogElement>(null);

  function onClick() {
    modalRef.current?.showModal();
  }

  const wrappedActionElement = <div onClick={onClick}>{actionElement}</div>;

  return (
    <React.Fragment>
      {wrappedActionElement}
      <dialog id={id} ref={modalRef} className="modal">
        <div className="modal-box">{children}</div>
      </dialog>
    </React.Fragment>
  );
}
