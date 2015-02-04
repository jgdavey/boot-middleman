require 'middleman-core/profiling'
Middleman::Profiling.start

require "middleman-core/load_paths"
Middleman.setup_load_paths

require "middleman-core/cli"

# Change directory to the root
Dir.chdir(ENV["MM_ROOT"]) if ENV["MM_ROOT"]

require "middleman-core/application"
Middleman::Cli::Build.shared_instance.set(:build_dir, ENV["MM_BUILD"])
Middleman::Cli::Build.new([]).invoke(:build)
