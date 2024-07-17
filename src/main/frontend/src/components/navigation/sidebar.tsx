export default function SideBar() {
  return (
    <aside className="left 0 fixed top-1/2 z-20 mx-1 hidden w-fit translate-y-[-50%] rounded px-2 py-4 sm:block">
      <div className="flex flex-col gap-4 rounded-lg bg-base-content bg-opacity-15 px-2 py-4">
        <div className="btn btn-secondary h-12 w-12">1</div>
        <div className="btn btn-secondary h-12 w-12">2</div>
        <div className="btn btn-secondary h-12 w-12">3</div>
        <div className="btn btn-secondary h-12 w-12">4</div>
      </div>
    </aside>
  );
}
