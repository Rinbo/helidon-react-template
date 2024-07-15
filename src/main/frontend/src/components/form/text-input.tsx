import React from "react";

type Props = { register: any; placeholder?: string; error?: string };

export default function TextInput({ register, placeholder, error }: Props) {
  return (
    <React.Fragment>
      <input className={`error input input-bordered ${error && "input-error"}`} placeholder={placeholder} {...register} />
      {error && <p className="text-xs text-error">{error}</p>}
    </React.Fragment>
  );
}
