def jobsMapping = [
  tags: [jobName:"App GWAPortal", jobTags: "reload", extraVars: "app_generic_image_tag: latest"],
  master: [jobName:"App GWAPortal", jobTags: "reload", extraVars: "app_generic_image_tag: master"]
]

def extraImages =
[
    [imageName: "gwaportal-backend", dockerFile: "Dockerfile", dockerContext: ".", extraBuildArgs: '--target aragwas-backend'],
    [imageName: "aragwas-worker", test: null, dockerFile: "Dockerfile", dockerContext: ".", extraBuildArgs: '--target aragwas-worker']
]

buildDockerImage([
    imageName: "gwaportal-backend",
    pushRegistryNamespace: "nordborglab/gwaportal",
    pushBranches: ['develop','master', 'refactor'],
    tower: jobsMapping
])