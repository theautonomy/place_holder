## Using docker
* docker build -f dockerfile-mkdocs -t gitbook-test .

* docker run -p 9000:9000 gitbook-test

## Command line
* python -m pip install mkdocs-material-extensions>=1.0
* python -m mkdocs build
* python -m mkdocs serve