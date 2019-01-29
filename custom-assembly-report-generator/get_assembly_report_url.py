from ftplib import FTP
import argparse
import re


def get_assembly_report_url(assembly_accession):
    if re.match("^GC[F|A]_\d+\.\d+$", assembly_accession) is not None:
        raise Exception('ERROR: Invalid assembly accession: it has to be in the form of '
                        'GCF_XXXXXXXXX.X or GCA_XXXXXXXXX.X where X is a number')

    ftp = FTP('ftp.ncbi.nlm.nih.gov')
    ftp.login()

    genome_folder = 'genomes/all/' + '/'.join([assembly_accession[0:3], assembly_accession[4:7],
                                               assembly_accession[7:10], assembly_accession[10:13]]) + '/'
    ftp.cwd(genome_folder)

    all_genome_subfolders = []
    ftp.retrlines('NLST', lambda line: all_genome_subfolders.append(line))

    genome_subfolders = [folder for folder in all_genome_subfolders if assembly_accession in folder]

    if len(genome_subfolders) != 1:
        raise Exception('more than one folder matches the assembly accession: ' + str(genome_subfolders))

    ftp.cwd(genome_subfolders[0])
    genome_files = []
    ftp.retrlines('NLST', lambda line: genome_files.append(line))

    assembly_reports = [genome_file for genome_file in genome_files if 'assembly_report' in genome_file]
    if len(assembly_reports) != 1:
        raise Exception('more than one file has "assembly_report" in its name: ' + str(assembly_reports))

    return 'ftp://' + 'ftp.ncbi.nlm.nih.gov' + '/' + genome_folder + genome_subfolders[0] + '/' + assembly_reports[0]


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Retrieve the assembly report of a given assembly accession.')
    parser.add_argument('-a', dest='assembly_accession', required=True, help='GCF_XXXXXXXXX.X or GCA_XXXXXXXXX.X '
                                                                             'where X is a number')
    args = parser.parse_args()
    print(get_assembly_report_url(args.assembly_accession))
