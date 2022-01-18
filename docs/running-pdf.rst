.. _rstrunningpdf:

===================================
Running isopret: Exporting PDF file
===================================

Ispopret allows users to export PDF files of the
isoform or protein domain graphics created by isorpret
for each analyzed gene. To create the PDF file,
isopret first creates a SVG file and then uses
the program ``rsvg-convert <https://helpmanual.io/help/rsvg-convert/>`_
to convert the SVG file to a PDF file (if desired,
the SVG file can be saved itself).

Isopret creates a temporary file, writes the SVG to this
file, and then starts a system process to run rsvg-convert.
Thus, rsvg-convert must be available on your PATH. On most
systems, it is sufficient to install the program system wide.

If isopret cannot find rsvg-convert, it will show an error dialog.
In this case, you can save the SVG file and convert it to PDF using
any method of choice.


Installing rsvg-convert
#######################

Macintosh
^^^^^^^^^

The easiest way to install the software is with brew. ::

    brew install librsvg

Debian-flavors of linux
^^^^^^^^^^^^^^^^^^^^^^^

On Ubuntu and other linuxes with apt-get, use the following command. ::

    apt-get install librsvg2-bin

Running rsvg-convert
####################

If you are not able to get isopret to use rsvg-convert, but have it on your system
and have saved an SVG file, this is the command that is used to
convert the file to PDF. ::

    commands = rsvg-convert -f pdf -o myfilename.pdf myfilename.svg

myfilename is the base file name of the SVG file and of the PDF file that will be created.